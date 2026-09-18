# docs/DEPLOY-AWS.md 참고 — EC2(t3.micro) 위에서 docker compose build로 직접 빌드한다.
# eclipse-temurin 공식 이미지가 amd64/arm64 멀티아치를 지원해서 별도 buildx 설정 없이도
# EC2 아키텍처에 맞는 이미지가 자동으로 받아진다.

# ---- 1단계: Gradle 빌드 ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# 의존성 캐시 레이어를 소스보다 먼저 분리 — build.gradle이 안 바뀌면 이 레이어가 재사용된다.
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew

COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# ---- 2단계: 런타임 (JDK 대신 JRE만 - 이미지 크기 축소) ----
FROM eclipse-temurin:21-jre AS run
WORKDIR /app

RUN useradd --system --create-home --home-dir /app appuser
COPY --from=build /workspace/build/libs/*.jar app.jar
USER appuser

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

# t3.micro(메모리 1GB)라 컨테이너 메모리 인식 + 힙 상한을 명시적으로 걸어둔다.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70.0", "-jar", "/app/app.jar"]

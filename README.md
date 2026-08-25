# 두리번 (Duribun) 🧭

> **GPS 위치 인증으로 여행을 "플레이"하는 게임형 여행 플랫폼 백엔드.**
> 방문 인증을 시계열로 누적하고, 그 기록이 다시 **캐릭터 지급·배지·포인트**로 되먹임되는 Spring Boot 서버입니다.

한국 관광의 서울·부산 편중 현상을 완화하기 위해, 사용자가 시군구를 실제로 방문·인증하면 그 지역 캐릭터를 얻고 도감을 채워가는 위치 기반 게임형 서비스입니다. "왜 그냥 여행 앱이 아니라 게임인가?"에 대한 답은 **위치 인증이라는 단일 행위가 캐릭터 획득·배지·포인트라는 여러 보상으로 자동 확장되는 이벤트 기반 구조**입니다.

---

## 목차
- [무엇을 만들었나](#무엇을-만들었나)
- [왜 이렇게 설계했나 (핵심 의사결정)](#왜-이렇게-설계했나-핵심-의사결정)
- [아키텍처](#아키텍처)
- [핵심 루프: 인증 → 이벤트 → 보상](#핵심-루프-인증--이벤트--보상)
- [도메인 및 API 현황](#도메인-및-api-현황)
- [기술 스택](#기술-스택)
- [로컬 실행](#로컬-실행)
- [DB 마이그레이션 (Flyway)](#db-마이그레이션-flyway)
- [검증 / 테스트](#검증--테스트)
- [프로젝트 구조](#프로젝트-구조)

---

## 무엇을 만들었나

모바일 앱(iOS/Android)이 소셜 로그인 SDK로 로그인한 뒤, 이 서버와 대화하며 여행을 "플레이"합니다.

- **로그인하고** — Google/Kakao/Naver 소셜 로그인. 앱이 각 플랫폼 SDK로 받은 토큰을 서버로 보내면, 서버가 이를 검증하고 자체 JWT를 발급
- **인증하고** — 특정 지역 반경 안에서 GPS 좌표를 보내면, 서버가 Haversine 거리 계산으로 방문 여부를 판정
- **보상받는다** — 인증 성공은 **하나의 이벤트**로 발행되고, 이를 구독하는 여러 도메인이 각자 반응해 캐릭터를 지급하고, 방문 지역 수에 따라 배지를 채점
- **모으고 꾸민다** — 도감(캐릭터), 포인트로 아이템을 사서 캐릭터 커스터마이징, 여행 기록(사진·감상) 작성
- **주변을 탐색한다** — 한국관광공사 TourAPI를 연동해 방문 지역/현재 위치 주변의 실제 관광지 정보를 조회

---

## 왜 이렇게 설계했나 (핵심 의사결정)

### 1. 위치 인증의 결과는 "호출"이 아니라 "이벤트"로 퍼진다
위치 인증에 성공하면 캐릭터 지급(Character), 배지 판정(Badge)이 뒤따라야 합니다. `location` 도메인이 이 둘을 직접 호출하는 대신, `LocationVerifiedEvent(userId, regionId, isFirstVisit)`를 `ApplicationEventPublisher`로 발행하고 각 도메인이 구독하는 구조를 택했습니다. **왜?**
- 두 명이 나눠 개발하는 프로젝트에서, `location`을 담당하지 않는 사람이 `location` 패키지 내부를 몰라도 이벤트 클래스 하나만 보고 리스너를 만들 수 있어야 병렬 개발이 막히지 않습니다.
- 재방문(이미 인증된 지역)은 이벤트를 아예 발행하지 않아, "중복 지급 방지"를 이벤트 발행 시점에서 원천 차단합니다 — 구독하는 쪽마다 중복 체크 로직을 반복할 필요가 없습니다.

### 2. 거리 계산은 PostGIS가 아니라 애플리케이션 레벨 Haversine
지역 인증 반경 판정에 지리정보 확장(PostGIS)을 쓰는 대신, 순수 Java로 Haversine 공식을 구현했습니다. 초기 트래픽 규모에서는 공간 인덱스가 주는 이점보다 **인프라 구성 비용**이 더 크다고 판단했기 때문입니다.

### 3. 도메인 간에는 FK가 아니라 값(userId)으로만 연결
`VisitRecord`, `PointHistory`, `UserBadge` 등 어떤 엔티티도 `User`를 `@ManyToOne`으로 참조하지 않고 `userId: Long` 값만 가집니다. 도메인 경계를 흐리는 가장 흔한 원인이 엔티티 간 직접 연관관계라고 판단해, 처음부터 값 참조로 통일했습니다.

### 4. 같은 "소셜 로그인"도 provider마다 검증 방식이 다르다 — 그래도 인터페이스는 통일
Google의 ID Token은 서명된 JWT라 우리가 직접 서명·audience를 검증해야 하지만(`google-api-client`), Kakao/Naver의 Access Token은 provider 서버가 대신 검증해주므로 단순 REST 호출로 충분합니다. 이 차이를 없애려고 Google까지 Access Token 방식으로 맞추는 방안도 검토했지만, Google 공식 문서가 ID Token 방식을 권장하고 프론트 SDK도 ID Token을 기본으로 반환하기 때문에 **토큰 타입은 provider별로 다르게 두고, `SocialAuthClient` 인터페이스로 바깥에서 보이는 형태만 통일**했습니다.

```java
public interface SocialAuthClient {
    SocialUserInfo getUserInfo(String token);
}
```

### 5. 서로 다른 코드 체계는 "변환"이 아니라 "매핑 데이터"로 해결
`Region`은 법정동코드(예: 강릉시=51150)를, 한국관광공사 TourAPI는 자체 지역코드(예: 강원=32, 강릉=1)를 씁니다. 두 체계는 산술적으로 변환할 수 없어서, `map` 도메인 안에 `TourApiRegionMapping`이라는 별도 매핑 엔티티를 두고 **이름 기준 자동 매칭 + 실패분 수동 보정**으로 채웠습니다. `location` 도메인은 이 문제 때문에 건드리지 않았습니다 — 코드 체계 불일치는 이를 필요로 하는 쪽(map)이 흡수해야 한다는 원칙입니다.

### 6. 포인트는 비관적 락이 아니라 낙관적 락
동일 유저의 포인트 계정에 밀리초 단위로 동시 요청이 몰릴 확률은 낮다고 보고, `PointAccount`에 `@Version` 기반 낙관적 락을 적용했습니다. 비관적 락(`SELECT ... FOR UPDATE`)은 흔치 않은 충돌을 막으려고 매 요청마다 잠금 비용을 지불하는 셈이라, 이 도메인 특성에는 맞지 않다고 판단했습니다.

---

## 아키텍처

```
┌──────────────┐  소셜 로그인 SDK   ┌──────────────────────────────────────────┐
│ 모바일 앱      │ ────────────────▶ │            두리번 백엔드 (Spring Boot)        │
│ (iOS/Android)│ ◀──────────────── │                                            │
└──────────────┘   JWT / API 응답   │  ┌────────┐ ┌──────────┐ ┌────────────┐  │
                                    │  │  auth   │ │ location │ │    map     │  │
                                    │  └────────┘ └────┬─────┘ └─────┬──────┘  │
                                    │                   │ event        │ REST   │
                                    │              ┌────▼─────┐  ┌────▼─────┐  │
                                    │              │ character │  │ TourAPI  │──┼──▶ 한국관광공사
                                    │              │  badge    │  └──────────┘  │
                                    │              └───────────┘                │
                                    │  ┌────────┐ ┌──────────┐ ┌────────────┐  │
                                    │  │  point  │ │   shop   │ │   record   │  │
                                    │  └────────┘ └──────────┘ └────────────┘  │
                                    └──────────────────┬─────────────────────┘
                                                        │ JPA
                                                        ▼
                                                 ┌──────────────┐
                                                 │ PostgreSQL   │
                                                 └──────────────┘
```

- `location`은 `character`/`badge`를 알지 못합니다. `LocationVerifiedEvent`만 발행하고, 구독은 받는 쪽의 책임입니다.
- `map`은 `location`의 `Region`을 읽기 전용으로만 참조합니다.
- 모든 도메인 엔티티는 `common/entity`의 `BaseTimeEntity`(또는 생성자/수정자까지 필요하면 `BaseEntity`)를 상속해 `createdAt`/`updatedAt`을 자동 관리합니다.

---

## 핵심 루프: 인증 → 이벤트 → 보상

이 프로젝트의 정체성인 되먹임 구조를 단계별로 보면:

### 1) 인증 (`POST /api/locations/verify`)
- 앱이 GPS 좌표와 `regionId`를 보내면, `Region` 기준 좌표와의 거리를 Haversine으로 계산
- 반경 이내면 `VisitRecord` 생성(최초 방문에 한해), `LocationVerifiedEvent` 발행
- 반경 밖이어도 에러가 아니라 `verified:false` + 거리값으로 정상 응답 — "인증 시도" 자체는 유효한 요청이라는 판단

### 2) 이벤트 전파
- `LocationVerifiedEvent(isFirstVisit=true)`만 의미 있는 이벤트로 취급, 재방문은 구독자들에게 아예 전달되지 않음
- `badge`는 이 이벤트만으로 자체 카운터(`UserVisitCounter`)를 올리고, 기준치를 넘는 미획득 배지를 한 번에 지급
- `character`는 같은 이벤트로 해당 지역 캐릭터를 지급 (location 내부 테이블을 조회하지 않음)

### 3) 보상 확인
- `GET /api/badges/me`, `GET /api/characters/me`로 누적된 결과를 즉시 확인 가능
- 포인트는 아직 이 루프에 자동 연결되어 있지 않습니다 — 어떤 행위(관광지 방문? 특산물 수집?)가 포인트를 트리거할지는 별도 도메인이 정해진 뒤 연결할 예정이며, 그 전까지 `PointService` 인터페이스만 노출해 `shop` 등 다른 도메인이 먼저 개발을 진행할 수 있게 했습니다.

---

## 도메인 및 API 현황

| 도메인 | 상태 | 설명 |
|---|---|---|
| `auth` | ✅ | Google/Kakao/Naver 소셜 로그인(모바일 SDK 토큰 검증), JWT 발급/재발급 |
| `user` | ✅ | 유저 정보, 닉네임 |
| `location` | ✅ | GPS 기반 위치 인증, `LocationVerifiedEvent` 발행 |
| `point` | ✅ | 포인트 적립/차감 (공통 모듈, 트리거는 미연결) |
| `badge` | ✅ | 방문 지역 수 기반 배지 자동 지급 (`location` 이벤트 구독) |
| `map` | ✅ | 한국관광공사 TourAPI 연동, 지역별/위치기반 관광지 조회·검색 |
| `setting` | ✅ | 알림 설정, 회원 탈퇴 |
| `shop` | 🔄 진행 중 | 포인트 기반 아이템 구매, 캐릭터 커스터마이징 |
| `character` | ⏳ 예정 | 지역 캐릭터 도감 (`location` 이벤트 구독) |
| `record` | ⏳ 예정 | 여행 기록 (사진·감상) |

주요 엔드포인트:

| 엔드포인트 | 설명 |
|---|---|
| `POST /api/auth/login/{provider}` | 소셜 로그인, JWT 발급 |
| `POST /api/auth/reissue` | 토큰 재발급 |
| `GET /api/locations/regions` | 지역 기준 정보 목록 |
| `POST /api/locations/verify` | GPS 좌표 기반 위치 인증 |
| `GET /api/locations/visits` | 내 방문 지역 목록 |
| `GET /api/points/me` / `GET /api/points/history` | 포인트 잔액/내역 |
| `GET /api/badges` / `GET /api/badges/me` | 배지 목록/내 배지 |
| `GET /api/map/regions/{regionId}/attractions` | 지역 관광지 목록 (TourAPI 연동) |
| `GET /api/map/nearby` | 주변 관광지 (위치 기반) |
| `GET /api/settings/me` | 알림 설정 조회/변경, 회원 탈퇴 |

---

## 기술 스택

| 영역 | 선택 | 이유 |
|---|---|---|
| Language / Runtime | **Java 21** | |
| Framework | **Spring Boot 4.1.0** | 모듈화된 스타터 체계(webmvc, data-jpa 등 기술별 분리) |
| Persistence | **Spring Data JPA / PostgreSQL** | |
| Auth | **Spring Security, JWT(jjwt)** | `oauth2Login` 리다이렉트 대신 모바일 SDK 토큰 검증 방식 채택 |
| 외부 연동 | **한국관광공사 TourAPI 4.0** | |
| Docs | **springdoc-openapi (Swagger UI)** | |
| Infra (로컬) | **Docker / Docker Compose** | PostgreSQL 로컬 구동 |
| Dev | **Lombok, Spring Boot DevTools** | |

---

## 로컬 실행

```bash
# 1) 환경변수 설정 (.env.example 참고, 실제 값은 팀 내부 공유)
cp .env.example .env

# 2) DB 실행
docker-compose up -d

# 3) 서버 실행 (dev 프로필)
./gradlew bootRun
```

Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## DB 마이그레이션 (Flyway)

prod 프로필은 `ddl-auto: validate`라 스키마를 자동 생성하지 않습니다. 실제 스키마는 `src/main/resources/db/migration`의 Flyway 마이그레이션(`V1__baseline.sql`, `V2__seed_reference_data.sql`, ...)으로만 갖춰집니다.

dev 프로필은 개발 속도를 위해 여전히 `ddl-auto: update`를 유지하지만, 이 때문에 dev와 prod 스키마가 어긋나지 않도록 **엔티티를 변경하는 PR에는 반드시 대응하는 마이그레이션 파일(`V{n}__description.sql`)을 함께 추가**합니다.

---

## 검증 / 테스트

자동화된 통합 테스트 스위트는 아직 없습니다. 대신 각 도메인마다 실제 요청/응답 기준의 **검증 시나리오 문서**를 두고, 구현 직후 수동으로 확인하는 방식을 취하고 있습니다.

자동화 테스트는 도메인 구현이 안정화된 뒤 추가할 예정입니다.

---

## 프로젝트 구조

```
duribun.be/
├── common/
│   ├── entity/       # BaseTimeEntity, BaseEntity
│   └── config/        # JpaAuditingConfig, AuditorAwareImpl
├── global/
│   ├── security/     # JWT 필터, SecurityConfig
│   └── exception/    # GlobalExceptionHandler
└── domain/
    ├── auth/          # 소셜 로그인, JWT
    ├── user/          # 유저 정보
    ├── location/      # 위치 인증, LocationVerifiedEvent 발행
    ├── point/         # 포인트 (공통 모듈)
    ├── badge/         # 배지 (Location 이벤트 구독)
    ├── map/            # TourAPI 연동, 지역코드 매핑
    ├── setting/        # 설정
    ├── character/      # 캐릭터 도감 (Location 이벤트 구독)
    ├── shop/            # 상점
    └── record/          # 여행 기록
```

---
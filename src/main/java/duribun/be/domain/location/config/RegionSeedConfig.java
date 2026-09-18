package duribun.be.domain.location.config;

import duribun.be.domain.location.entity.Region;
import duribun.be.domain.location.repository.RegionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

@Configuration
public class RegionSeedConfig {

    @Bean
    @Profile("dev")
    @Order(1)
    public CommandLineRunner regionSeedRunner(RegionRepository regionRepository) {
        // docs/ISSUE-지역마스코트시드-실데이터교체.md — 기존 서울/강릉은 처음부터 테스트용 예시 데이터였다
        // (Figma "(3) 위치 인증" 플로우, node 312:133 기준 실제 기획은 아래 10개 지역).
        //
        // sigunguCode/좌표는 초안이다. 광역시 코드는 "시도코드+000" 패턴으로 추정해 채웠고 좌표는 시청
        // 소재지 기준 — 정식 반영 전 행정표준코드관리시스템(code.go.kr)과 실좌표로 재검증 필요.
        // 독도는 별도 시군구 코드가 없어(행정구역상 경북 울릉군 울릉읍 소속) 울릉도(47430)와 겹치지 않게
        // 임의로 47431을 부여했다 — findBySigunguCode()가 단일 결과를 기대하는 조회라 두 지역이 코드를
        // 공유하면 조회가 깨진다. 실제 공식 코드 체계와는 무관한 내부용 placeholder이니 유의할 것.
        return args -> {
            if (regionRepository.count() > 0) {
                return;
            }
            regionRepository.save(Region.create("47431", "독도", 37.2416, 131.8631, 1000));
            regionRepository.save(Region.create("11000", "서울특별시", 37.5665, 126.9780, 1000));
            regionRepository.save(Region.create("31000", "울산광역시", 35.5384, 129.3114, 1000));
            regionRepository.save(Region.create("47430", "울릉군", 37.4845, 130.9057, 1000));
            regionRepository.save(Region.create("26000", "부산광역시", 35.1796, 129.0756, 1000));
            regionRepository.save(Region.create("30000", "대전광역시", 36.3504, 127.3845, 1000));
            regionRepository.save(Region.create("27000", "대구광역시", 35.8714, 128.6014, 1000));
            regionRepository.save(Region.create("29000", "광주광역시", 35.1595, 126.8526, 1000));
            regionRepository.save(Region.create("28000", "인천광역시", 37.4563, 126.7052, 1000));
            regionRepository.save(Region.create("50000", "제주특별자치도", 33.4996, 126.5312, 1000));
        };
    }
}

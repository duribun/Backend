package duribun.be.domain.map.seeder;

import duribun.be.domain.location.entity.Region;
import duribun.be.domain.location.repository.RegionRepository;
import duribun.be.domain.map.repository.TourApiRegionMappingRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// 수동 실행 전용 (docs/map.md §5) - 실제 TourAPI 네트워크 호출이 발생하므로 CI/일반 테스트 실행에는 포함하지 않는다.
// .env에 유효한 TOUR_API_SERVICE_KEY가 설정된 상태에서 @Disabled를 지우고 이 클래스만 개별 실행할 것.
// 주의: spring-dotenv 4.0.0이 Spring Boot 4.1.0과 바이너리 비호환(ConfigurableBootstrapContext 패키지 변경)이라
// 현재 .env가 로드되지 않는다 - 이 문제가 해결되기 전까지는 VM 옵션 등으로 TOUR_API_SERVICE_KEY를 직접 주입해야 한다.
@SpringBootTest
@ActiveProfiles("test")
@Disabled("수동 실행 전용 - 실제 TourAPI 호출 필요 (.env 로딩 이슈 해결 전까지는 TOUR_API_SERVICE_KEY를 환경변수로 직접 주입할 것)")
class TourApiRegionMappingSeederManualRunnerTest {

    @Autowired
    private TourApiRegionMappingSeeder seeder;
    @Autowired
    private RegionRepository regionRepository;
    @Autowired
    private TourApiRegionMappingRepository tourApiRegionMappingRepository;

    @Test
    void 실제_TourAPI를_호출해_강릉_서울_지역코드를_매칭한다() {
        Region gangneung = regionRepository.save(Region.create("51150", "강릉시", 37.7519, 128.8761, 1000));
        Region seoul = regionRepository.save(Region.create("11000", "서울특별시", 37.5665, 126.9780, 1000));

        seeder.seed();

        tourApiRegionMappingRepository.findByRegionId(gangneung.getId()).ifPresentOrElse(
                m -> System.out.println("강릉 매핑 성공: areaCode=" + m.getTourApiAreaCode()
                        + ", sigunguCode=" + m.getTourApiSigunguCode()),
                () -> System.out.println("강릉 매핑 실패 - 수동 확인 필요"));
        tourApiRegionMappingRepository.findByRegionId(seoul.getId()).ifPresentOrElse(
                m -> System.out.println("서울 매핑 성공: areaCode=" + m.getTourApiAreaCode()
                        + ", sigunguCode=" + m.getTourApiSigunguCode()),
                () -> System.out.println("서울 매핑 실패 - 수동 확인 필요"));
    }
}

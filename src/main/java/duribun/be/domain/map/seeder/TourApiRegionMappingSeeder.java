package duribun.be.domain.map.seeder;

import duribun.be.domain.location.entity.Region;
import duribun.be.domain.location.repository.RegionRepository;
import duribun.be.domain.map.client.TourApiClient;
import duribun.be.domain.map.dto.TourApiAreaCodeResponse;
import duribun.be.domain.map.entity.TourApiRegionMapping;
import duribun.be.domain.map.repository.TourApiRegionMappingRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// 상시 배치가 아닌 1회성 수동 실행용 (docs/map.md §5) — 실행 방법은 TourApiRegionMappingSeederManualRunnerTest 참고
@Component
public class TourApiRegionMappingSeeder {

    private static final Logger log = LoggerFactory.getLogger(TourApiRegionMappingSeeder.class);

    private static final List<String> ADMINISTRATIVE_SUFFIXES = List.of(
            "특별자치시", "특별자치도", "광역시", "특별시", "자치시", "자치군", "시", "군", "구"
    );

    private final TourApiClient tourApiClient;
    private final RegionRepository regionRepository;
    private final TourApiRegionMappingRepository tourApiRegionMappingRepository;

    public TourApiRegionMappingSeeder(TourApiClient tourApiClient, RegionRepository regionRepository,
                                       TourApiRegionMappingRepository tourApiRegionMappingRepository) {
        this.tourApiClient = tourApiClient;
        this.regionRepository = regionRepository;
        this.tourApiRegionMappingRepository = tourApiRegionMappingRepository;
    }

    @Transactional
    public void seed() {
        List<TourApiAreaCodeResponse> provinces = tourApiClient.areaCode(null);
        for (Region region : regionRepository.findAll()) {
            if (tourApiRegionMappingRepository.findByRegionId(region.getId()).isPresent()) {
                continue;
            }
            matchRegion(region, provinces);
        }
    }

    private void matchRegion(Region region, List<TourApiAreaCodeResponse> provinces) {
        String target = normalize(region.getName());

        for (TourApiAreaCodeResponse province : provinces) {
            if (normalize(province.name()).equals(target)) {
                save(region.getId(), province.code(), null);
                return;
            }
        }

        for (TourApiAreaCodeResponse province : provinces) {
            for (TourApiAreaCodeResponse sigungu : tourApiClient.areaCode(province.code())) {
                if (normalize(sigungu.name()).equals(target)) {
                    save(region.getId(), province.code(), sigungu.code());
                    return;
                }
            }
        }

        log.warn("TourAPI 지역코드 자동 매칭 실패 - 수동 확인 필요: regionId={}, name={}", region.getId(), region.getName());
    }

    private void save(Long regionId, String areaCode, String sigunguCode) {
        tourApiRegionMappingRepository.save(TourApiRegionMapping.create(regionId, areaCode, sigunguCode));
    }

    private String normalize(String name) {
        if (name == null) {
            return "";
        }
        String stripped = name.replaceAll("\\s+", "");
        for (String suffix : ADMINISTRATIVE_SUFFIXES) {
            if (stripped.endsWith(suffix) && stripped.length() > suffix.length()) {
                return stripped.substring(0, stripped.length() - suffix.length());
            }
        }
        return stripped;
    }
}

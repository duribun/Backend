package duribun.be.domain.map.repository;

import duribun.be.common.config.JpaAuditingConfig;
import duribun.be.domain.map.entity.TourApiRegionMapping;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
class TourApiRegionMappingRepositoryTest {

    @Autowired
    private TourApiRegionMappingRepository tourApiRegionMappingRepository;

    @Test
    void findByRegionId_존재하는_매핑을_조회한다() {
        tourApiRegionMappingRepository.saveAndFlush(TourApiRegionMapping.create(1L, "32", "1"));

        Optional<TourApiRegionMapping> found = tourApiRegionMappingRepository.findByRegionId(1L);

        assertThat(found).isPresent();
        assertThat(found.get().getTourApiAreaCode()).isEqualTo("32");
    }

    @Test
    void findByRegionId_존재하지_않으면_빈값을_반환한다() {
        Optional<TourApiRegionMapping> found = tourApiRegionMappingRepository.findByRegionId(999L);

        assertThat(found).isEmpty();
    }
}

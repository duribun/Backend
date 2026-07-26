package duribun.be.domain.location.repository;

import duribun.be.common.config.JpaAuditingConfig;
import duribun.be.domain.location.entity.Region;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
class RegionRepositoryTest {

    @Autowired
    private RegionRepository regionRepository;

    @Test
    void save하면_createdAt_updatedAt이_자동으로_채워진다() {
        Region region = Region.create("51150", "강릉시", 37.7519, 128.8761, 1000);

        Region saved = regionRepository.saveAndFlush(region);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findAll로_저장된_지역_전체를_조회한다() {
        regionRepository.saveAndFlush(Region.create("51150", "강릉시", 37.7519, 128.8761, 1000));
        regionRepository.saveAndFlush(Region.create("11000", "서울특별시", 37.5665, 126.9780, 1000));

        assertThat(regionRepository.findAll()).hasSize(2);
    }
}

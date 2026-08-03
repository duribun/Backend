package duribun.be.domain.map.repository;

import duribun.be.common.config.JpaAuditingConfig;
import duribun.be.domain.map.entity.Attraction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
class AttractionRepositoryTest {

    @Autowired
    private AttractionRepository attractionRepository;

    @Test
    void save하면_createdAt_updatedAt이_자동으로_채워진다() {
        Attraction attraction = Attraction.create("12345", 1L, "12", "경포대", "강원 강릉시",
                37.79, 128.90, null, null);

        Attraction saved = attractionRepository.saveAndFlush(attraction);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByContentId_존재하는_관광지를_조회한다() {
        attractionRepository.saveAndFlush(Attraction.create("12345", 1L, "12", "경포대", "강원 강릉시",
                37.79, 128.90, null, null));

        Optional<Attraction> found = attractionRepository.findByContentId("12345");

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("경포대");
    }

    @Test
    void findByContentId_존재하지_않으면_빈값을_반환한다() {
        Optional<Attraction> found = attractionRepository.findByContentId("no-such-id");

        assertThat(found).isEmpty();
    }

    @Test
    void findByRegionId_해당_지역의_관광지_목록을_조회한다() {
        attractionRepository.saveAndFlush(Attraction.create("1", 1L, "12", "A", "주소",
                1.0, 1.0, null, null));
        attractionRepository.saveAndFlush(Attraction.create("2", 1L, "12", "B", "주소",
                1.0, 1.0, null, null));
        attractionRepository.saveAndFlush(Attraction.create("3", 2L, "12", "C", "주소",
                1.0, 1.0, null, null));

        List<Attraction> found = attractionRepository.findByRegionId(1L);

        assertThat(found).hasSize(2)
                .extracting(Attraction::getTitle)
                .containsExactlyInAnyOrder("A", "B");
    }
}

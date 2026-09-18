package duribun.be.domain.location.repository;

import duribun.be.common.config.JpaAuditingConfig;
import duribun.be.domain.location.entity.VisitRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
class VisitRecordRepositoryTest {

    @Autowired
    private VisitRecordRepository visitRecordRepository;

    @Test
    void save하면_createdAt_updatedAt이_자동으로_채워진다() {
        VisitRecord visitRecord = VisitRecord.create(1L, 1L, LocalDateTime.now());

        VisitRecord saved = visitRecordRepository.saveAndFlush(visitRecord);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByUserIdAndRegionId_존재하는_방문기록을_조회한다() {
        visitRecordRepository.saveAndFlush(VisitRecord.create(1L, 2L, LocalDateTime.now()));

        Optional<VisitRecord> found = visitRecordRepository.findByUserIdAndRegionId(1L, 2L);

        assertThat(found).isPresent();
    }

    @Test
    void findByUserIdAndRegionId_존재하지_않으면_빈값을_반환한다() {
        Optional<VisitRecord> found = visitRecordRepository.findByUserIdAndRegionId(1L, 999L);

        assertThat(found).isEmpty();
    }

    @Test
    void findByUserId_해당_유저의_방문기록만_조회한다() {
        visitRecordRepository.saveAndFlush(VisitRecord.create(1L, 1L, LocalDateTime.now()));
        visitRecordRepository.saveAndFlush(VisitRecord.create(1L, 2L, LocalDateTime.now()));
        visitRecordRepository.saveAndFlush(VisitRecord.create(2L, 1L, LocalDateTime.now()));

        List<VisitRecord> found = visitRecordRepository.findByUserId(1L);

        assertThat(found).hasSize(2);
    }
}

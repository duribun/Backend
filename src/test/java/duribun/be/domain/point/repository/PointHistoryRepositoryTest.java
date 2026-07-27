package duribun.be.domain.point.repository;

import duribun.be.common.config.JpaAuditingConfig;
import duribun.be.domain.point.entity.PointHistory;
import duribun.be.domain.point.service.PointReason;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
class PointHistoryRepositoryTest {

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Test
    void save하면_createdAt_updatedAt이_자동으로_채워진다() {
        PointHistory history = PointHistory.create(1L, 100, PointReason.CHARACTER_COLLECT, 100);

        PointHistory saved = pointHistoryRepository.saveAndFlush(history);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByUserId_해당_유저의_내역만_페이징하여_조회한다() {
        pointHistoryRepository.saveAndFlush(PointHistory.create(1L, 100, PointReason.CHARACTER_COLLECT, 100));
        pointHistoryRepository.saveAndFlush(PointHistory.create(1L, -40, PointReason.SHOP_PURCHASE, 60));
        pointHistoryRepository.saveAndFlush(PointHistory.create(2L, 50, PointReason.PRODUCT_COLLECT, 50));

        Page<PointHistory> page = pointHistoryRepository.findByUserId(
                1L, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(PointHistory::getUserId).containsOnly(1L);
    }
}

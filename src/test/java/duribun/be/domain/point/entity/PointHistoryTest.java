package duribun.be.domain.point.entity;

import duribun.be.domain.point.service.PointReason;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PointHistoryTest {

    @Test
    void create_전달받은_필드로_내역을_생성한다() {
        PointHistory history = PointHistory.create(1L, 100, PointReason.CHARACTER_COLLECT, 100);

        assertThat(history.getUserId()).isEqualTo(1L);
        assertThat(history.getAmount()).isEqualTo(100);
        assertThat(history.getReason()).isEqualTo(PointReason.CHARACTER_COLLECT);
        assertThat(history.getBalanceAfter()).isEqualTo(100);
    }

    @Test
    void create_차감_내역은_amount가_음수로_저장된다() {
        PointHistory history = PointHistory.create(1L, -40, PointReason.SHOP_PURCHASE, 60);

        assertThat(history.getAmount()).isEqualTo(-40);
        assertThat(history.getBalanceAfter()).isEqualTo(60);
    }
}

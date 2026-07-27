package duribun.be.domain.point.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PointAccountTest {

    @Test
    void create_초기_잔액은_0이다() {
        PointAccount account = PointAccount.create(1L);

        assertThat(account.getUserId()).isEqualTo(1L);
        assertThat(account.getBalance()).isEqualTo(0);
    }

    @Test
    void increaseBalance_호출하면_잔액이_증가한다() {
        PointAccount account = PointAccount.create(1L);

        account.increaseBalance(100);

        assertThat(account.getBalance()).isEqualTo(100);
    }

    @Test
    void decreaseBalance_잔액이_충분하면_차감된다() {
        PointAccount account = PointAccount.create(1L);
        account.increaseBalance(100);

        account.decreaseBalance(40);

        assertThat(account.getBalance()).isEqualTo(60);
    }

    @Test
    void decreaseBalance_잔액보다_많으면_예외를_던진다() {
        PointAccount account = PointAccount.create(1L);
        account.increaseBalance(10);

        assertThatThrownBy(() -> account.decreaseBalance(20))
                .isInstanceOf(IllegalStateException.class);
        assertThat(account.getBalance()).isEqualTo(10);
    }
}

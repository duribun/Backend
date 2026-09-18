package duribun.be.domain.point.repository;

import duribun.be.common.config.JpaAuditingConfig;
import duribun.be.domain.point.entity.PointAccount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
class PointAccountRepositoryTest {

    @Autowired
    private PointAccountRepository pointAccountRepository;

    @Test
    void save하면_createdAt_updatedAt이_자동으로_채워진다() {
        PointAccount account = PointAccount.create(1L);

        PointAccount saved = pointAccountRepository.saveAndFlush(account);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByUserId_존재하는_계좌를_조회한다() {
        pointAccountRepository.saveAndFlush(PointAccount.create(1L));

        Optional<PointAccount> found = pointAccountRepository.findByUserId(1L);

        assertThat(found).isPresent();
    }

    @Test
    void findByUserId_존재하지_않으면_빈값을_반환한다() {
        Optional<PointAccount> found = pointAccountRepository.findByUserId(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void 저장된_버전보다_오래된_버전의_계좌를_저장하면_낙관적_락_예외가_발생한다() throws Exception {
        PointAccount account = pointAccountRepository.saveAndFlush(PointAccount.create(1L));
        Long accountId = account.getId();
        account.increaseBalance(100);
        pointAccountRepository.saveAndFlush(account);

        PointAccount staleCopy = PointAccount.create(1L);
        setField(staleCopy, "id", accountId);
        setField(staleCopy, "version", 0L);

        assertThatThrownBy(() -> pointAccountRepository.saveAndFlush(staleCopy))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = PointAccount.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

package duribun.be.domain.point.service;

import duribun.be.domain.point.dto.PointHistoryResponse;
import duribun.be.domain.point.entity.PointAccount;
import duribun.be.domain.point.entity.PointHistory;
import duribun.be.domain.point.repository.PointAccountRepository;
import duribun.be.domain.point.repository.PointHistoryRepository;
import duribun.be.global.exception.InsufficientPointException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointServiceImplTest {

    @Mock
    private PointAccountRepository pointAccountRepository;
    @Mock
    private PointHistoryRepository pointHistoryRepository;

    private PointServiceImpl pointService;

    @BeforeEach
    void setUp() {
        pointService = new PointServiceImpl(pointAccountRepository, pointHistoryRepository);
    }

    @Test
    void earn_계좌가_없으면_생성하고_잔액을_증가시킨다() {
        when(pointAccountRepository.findByUserId(1L)).thenReturn(Optional.empty());

        pointService.earn(1L, 100, PointReason.CHARACTER_COLLECT);

        ArgumentCaptor<PointAccount> accountCaptor = ArgumentCaptor.forClass(PointAccount.class);
        verify(pointAccountRepository).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getBalance()).isEqualTo(100);
    }

    @Test
    void earn_기존_계좌가_있으면_잔액에_더한다() {
        PointAccount account = PointAccount.create(1L);
        account.increaseBalance(50);
        when(pointAccountRepository.findByUserId(1L)).thenReturn(Optional.of(account));

        pointService.earn(1L, 100, PointReason.CHARACTER_COLLECT);

        assertThat(account.getBalance()).isEqualTo(150);
    }

    @Test
    void earn_호출시_History가_양수_금액으로_기록된다() {
        when(pointAccountRepository.findByUserId(1L)).thenReturn(Optional.empty());

        pointService.earn(1L, 100, PointReason.CHARACTER_COLLECT);

        ArgumentCaptor<PointHistory> historyCaptor = ArgumentCaptor.forClass(PointHistory.class);
        verify(pointHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getAmount()).isEqualTo(100);
        assertThat(historyCaptor.getValue().getBalanceAfter()).isEqualTo(100);
        assertThat(historyCaptor.getValue().getReason()).isEqualTo(PointReason.CHARACTER_COLLECT);
    }

    @Test
    void spend_잔액이_충분하면_차감된다() {
        PointAccount account = PointAccount.create(1L);
        account.increaseBalance(100);
        when(pointAccountRepository.findByUserId(1L)).thenReturn(Optional.of(account));

        pointService.spend(1L, 40, PointReason.SHOP_PURCHASE);

        assertThat(account.getBalance()).isEqualTo(60);
    }

    @Test
    void spend_호출시_History가_음수_금액으로_기록된다() {
        PointAccount account = PointAccount.create(1L);
        account.increaseBalance(100);
        when(pointAccountRepository.findByUserId(1L)).thenReturn(Optional.of(account));

        pointService.spend(1L, 40, PointReason.SHOP_PURCHASE);

        ArgumentCaptor<PointHistory> historyCaptor = ArgumentCaptor.forClass(PointHistory.class);
        verify(pointHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getAmount()).isEqualTo(-40);
        assertThat(historyCaptor.getValue().getBalanceAfter()).isEqualTo(60);
    }

    @Test
    void spend_잔액이_부족하면_예외를_던지고_변경이_없다() {
        PointAccount account = PointAccount.create(1L);
        account.increaseBalance(10);
        when(pointAccountRepository.findByUserId(1L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> pointService.spend(1L, 20, PointReason.SHOP_PURCHASE))
                .isInstanceOf(InsufficientPointException.class);

        assertThat(account.getBalance()).isEqualTo(10);
        verify(pointHistoryRepository, never()).save(any(PointHistory.class));
    }

    @Test
    void spend_계좌가_없으면_잔액_0으로_간주해_예외를_던진다() {
        when(pointAccountRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pointService.spend(1L, 10, PointReason.SHOP_PURCHASE))
                .isInstanceOf(InsufficientPointException.class);
    }

    @Test
    void earn_동시에_같은_유저의_계좌가_먼저_생성되면_재조회해_반영한다() {
        PointAccount racedAccount = PointAccount.create(1L);
        racedAccount.increaseBalance(30);
        when(pointAccountRepository.findByUserId(1L))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(racedAccount));
        when(pointAccountRepository.save(any(PointAccount.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate user_id"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        pointService.earn(1L, 100, PointReason.CHARACTER_COLLECT);

        assertThat(racedAccount.getBalance()).isEqualTo(130);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -10})
    void earn_금액이_0이하이면_예외를_던지고_아무것도_저장하지_않는다(int amount) {
        assertThatThrownBy(() -> pointService.earn(1L, amount, PointReason.CHARACTER_COLLECT))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(pointAccountRepository, pointHistoryRepository);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -10})
    void spend_금액이_0이하이면_예외를_던지고_아무것도_저장하지_않는다(int amount) {
        assertThatThrownBy(() -> pointService.spend(1L, amount, PointReason.SHOP_PURCHASE))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(pointAccountRepository, pointHistoryRepository);
    }

    @Test
    void getBalance_계좌가_없으면_0을_반환한다() {
        when(pointAccountRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThat(pointService.getBalance(1L)).isEqualTo(0);
    }

    @Test
    void getBalance_계좌가_있으면_잔액을_반환한다() {
        PointAccount account = PointAccount.create(1L);
        account.increaseBalance(70);
        when(pointAccountRepository.findByUserId(1L)).thenReturn(Optional.of(account));

        assertThat(pointService.getBalance(1L)).isEqualTo(70);
    }

    @Test
    void getHistory_페이징된_내역을_응답으로_변환한다() {
        PointHistory history = PointHistory.create(1L, 100, PointReason.CHARACTER_COLLECT, 100);
        Pageable pageable = PageRequest.of(0, 10);
        Page<PointHistory> page = new PageImpl<>(List.of(history), pageable, 1);
        when(pointHistoryRepository.findByUserId(1L, pageable)).thenReturn(page);

        Page<PointHistoryResponse> result = pointService.getHistory(1L, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).amount()).isEqualTo(100);
    }
}

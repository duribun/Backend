package duribun.be.domain.point.service;

import duribun.be.domain.mascot.event.MascotAcquiredEvent;
import duribun.be.domain.point.dto.PointHistoryResponse;
import duribun.be.domain.point.entity.PointAccount;
import duribun.be.domain.point.entity.PointHistory;
import duribun.be.domain.point.repository.PointAccountRepository;
import duribun.be.domain.point.repository.PointHistoryRepository;
import duribun.be.global.exception.InsufficientPointException;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PointServiceImpl implements PointService {

    private static final int MASCOT_ACQUIRED_REWARD = 20;

    private final PointAccountRepository pointAccountRepository;
    private final PointHistoryRepository pointHistoryRepository;

    public PointServiceImpl(PointAccountRepository pointAccountRepository,
                             PointHistoryRepository pointHistoryRepository) {
        this.pointAccountRepository = pointAccountRepository;
        this.pointHistoryRepository = pointHistoryRepository;
    }

    @EventListener
    public void handleMascotAcquired(MascotAcquiredEvent event) {
        earn(event.userId(), MASCOT_ACQUIRED_REWARD, PointReason.MASCOT_COLLECT);
    }

    @Override
    public void earn(Long userId, int amount, PointReason reason) {
        requirePositiveAmount(amount);
        PointAccount account = findOrCreateAccount(userId);
        account.increaseBalance(amount);
        try {
            pointAccountRepository.save(account);
        } catch (DataIntegrityViolationException e) {
            // 동시에 같은 유저의 계좌가 먼저 생성된 경우: 실제 계좌를 재조회해 다시 반영한다
            account = pointAccountRepository.findByUserId(userId).orElseThrow(() -> e);
            account.increaseBalance(amount);
            pointAccountRepository.save(account);
        }
        pointHistoryRepository.save(PointHistory.create(userId, amount, reason, account.getBalance()));
    }

    @Override
    public void spend(Long userId, int amount, PointReason reason) {
        requirePositiveAmount(amount);
        PointAccount account = findOrCreateAccount(userId);
        if (account.getBalance() < amount) {
            throw new InsufficientPointException("포인트 잔액이 부족합니다");
        }
        account.decreaseBalance(amount);
        pointAccountRepository.save(account);
        pointHistoryRepository.save(PointHistory.create(userId, -amount, reason, account.getBalance()));
    }

    @Override
    @Transactional(readOnly = true)
    public int getBalance(Long userId) {
        return pointAccountRepository.findByUserId(userId)
                .map(PointAccount::getBalance)
                .orElse(0);
    }

    @Transactional(readOnly = true)
    public Page<PointHistoryResponse> getHistory(Long userId, Pageable pageable) {
        return pointHistoryRepository.findByUserId(userId, pageable)
                .map(PointHistoryResponse::from);
    }

    private void requirePositiveAmount(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("금액은 0보다 커야 합니다");
        }
    }

    private PointAccount findOrCreateAccount(Long userId) {
        return pointAccountRepository.findByUserId(userId)
                .orElseGet(() -> PointAccount.create(userId));
    }
}

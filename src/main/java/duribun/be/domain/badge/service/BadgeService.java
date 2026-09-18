package duribun.be.domain.badge.service;

import duribun.be.domain.badge.dto.BadgeResponse;
import duribun.be.domain.badge.dto.MyBadgeResponse;
import duribun.be.domain.badge.entity.Badge;
import duribun.be.domain.badge.entity.UserBadge;
import duribun.be.domain.badge.entity.UserMascotCounter;
import duribun.be.domain.badge.event.BadgeAcquiredEvent;
import duribun.be.domain.badge.repository.BadgeRepository;
import duribun.be.domain.badge.repository.UserBadgeRepository;
import duribun.be.domain.badge.repository.UserMascotCounterRepository;
import duribun.be.domain.mascot.event.MascotAcquiredEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class BadgeService {

    private static final int MAX_MASCOT_COUNTER_RETRIES = 3;

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserMascotCounterRepository userMascotCounterRepository;
    private final ApplicationEventPublisher eventPublisher;

    public BadgeService(BadgeRepository badgeRepository,
                         UserBadgeRepository userBadgeRepository,
                         UserMascotCounterRepository userMascotCounterRepository,
                         ApplicationEventPublisher eventPublisher) {
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.userMascotCounterRepository = userMascotCounterRepository;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void handleMascotAcquired(MascotAcquiredEvent event) {
        int newCount = increaseMascotCounter(event.userId());

        List<Badge> newlyAcquiredBadges = badgeRepository
                .findByRequiredMascotCountLessThanEqual(newCount)
                .stream()
                .filter(badge -> !userBadgeRepository.existsByUserIdAndBadgeId(event.userId(), badge.getId()))
                .toList();

        newlyAcquiredBadges.forEach(badge -> {
            awardBadge(event.userId(), badge.getId());
            eventPublisher.publishEvent(new BadgeAcquiredEvent(event.userId(), badge.getId(), badge.getCode()));
        });
    }

    public List<BadgeResponse> getAllBadges(Long userId) {
        List<Badge> badges = badgeRepository.findAll();
        Map<Long, UserBadge> acquiredByBadgeId = userBadgeRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(UserBadge::getBadgeId, Function.identity()));

        return badges.stream()
                .map(badge -> BadgeResponse.of(badge, acquiredByBadgeId.get(badge.getId())))
                .toList();
    }

    /**
     * requiredMascotCount(등급 기준값) 내림차순으로 정렬해서 반환한다. 첫 항목이 곧 "현재 칭호"
     * (가장 높은 단계)이다. 마스코트 획득 수 임계값을 한 번에 여러 개 넘겨 여러 칭호가 같은 시각에
     * 부여된 경우 acquiredAt만으로는 최고 단계를 가려낼 수 없어서, 정렬 기준을 acquiredAt이 아닌
     * requiredMascotCount로 둔다.
     */
    public List<MyBadgeResponse> getMyBadges(Long userId) {
        List<UserBadge> userBadges = userBadgeRepository.findByUserId(userId);
        Map<Long, Badge> badgesById = badgeRepository
                .findAllById(userBadges.stream().map(UserBadge::getBadgeId).toList())
                .stream()
                .collect(Collectors.toMap(Badge::getId, Function.identity()));

        return userBadges.stream()
                .map(userBadge -> MyBadgeResponse.of(badgesById.get(userBadge.getBadgeId()), userBadge))
                .sorted(Comparator.comparing(MyBadgeResponse::requiredMascotCount).reversed())
                .toList();
    }

    private int increaseMascotCounter(Long userId) {
        int attempts = 0;
        while (true) {
            attempts++;
            UserMascotCounter counter = userMascotCounterRepository.findByUserId(userId)
                    .orElseGet(() -> UserMascotCounter.create(userId));
            counter.increase();
            try {
                userMascotCounterRepository.save(counter);
                return counter.getMascotCount();
            } catch (DataIntegrityViolationException | OptimisticLockingFailureException e) {
                // 동시에 같은 유저의 카운터가 먼저 생성/수정된 경우: 재조회 후 다시 반영을 재시도한다
                if (attempts >= MAX_MASCOT_COUNTER_RETRIES) {
                    throw e;
                }
            }
        }
    }

    private void awardBadge(Long userId, Long badgeId) {
        userBadgeRepository.save(UserBadge.create(userId, badgeId, LocalDateTime.now()));
    }
}

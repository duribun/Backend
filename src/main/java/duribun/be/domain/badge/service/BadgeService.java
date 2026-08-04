package duribun.be.domain.badge.service;

import duribun.be.domain.badge.dto.BadgeResponse;
import duribun.be.domain.badge.dto.MyBadgeResponse;
import duribun.be.domain.badge.entity.Badge;
import duribun.be.domain.badge.entity.UserBadge;
import duribun.be.domain.badge.entity.UserVisitCounter;
import duribun.be.domain.badge.repository.BadgeRepository;
import duribun.be.domain.badge.repository.UserBadgeRepository;
import duribun.be.domain.badge.repository.UserVisitCounterRepository;
import duribun.be.domain.location.event.LocationVerifiedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserVisitCounterRepository userVisitCounterRepository;

    public BadgeService(BadgeRepository badgeRepository,
                         UserBadgeRepository userBadgeRepository,
                         UserVisitCounterRepository userVisitCounterRepository) {
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.userVisitCounterRepository = userVisitCounterRepository;
    }

    @EventListener
    public void handleLocationVerified(LocationVerifiedEvent event) {
        if (!event.isFirstVisit()) {
            return;
        }

        int newCount = increaseVisitCounter(event.userId());

        List<Badge> newlyAcquiredBadges = badgeRepository
                .findByRequiredVisitCountLessThanEqual(newCount)
                .stream()
                .filter(badge -> !userBadgeRepository.existsByUserIdAndBadgeId(event.userId(), badge.getId()))
                .toList();

        newlyAcquiredBadges.forEach(badge -> awardBadge(event.userId(), badge.getId()));
    }

    public List<BadgeResponse> getAllBadges(Long userId) {
        List<Badge> badges = badgeRepository.findAll();
        Map<Long, UserBadge> acquiredByBadgeId = userBadgeRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(UserBadge::getBadgeId, Function.identity()));

        return badges.stream()
                .map(badge -> BadgeResponse.of(badge, acquiredByBadgeId.get(badge.getId())))
                .toList();
    }

    public List<MyBadgeResponse> getMyBadges(Long userId) {
        List<UserBadge> userBadges = userBadgeRepository.findByUserId(userId);
        Map<Long, Badge> badgesById = badgeRepository
                .findAllById(userBadges.stream().map(UserBadge::getBadgeId).toList())
                .stream()
                .collect(Collectors.toMap(Badge::getId, Function.identity()));

        return userBadges.stream()
                .map(userBadge -> MyBadgeResponse.of(badgesById.get(userBadge.getBadgeId()), userBadge))
                .toList();
    }

    private int increaseVisitCounter(Long userId) {
        UserVisitCounter counter = userVisitCounterRepository.findByUserId(userId)
                .orElseGet(() -> UserVisitCounter.create(userId));
        counter.increase();
        try {
            userVisitCounterRepository.save(counter);
        } catch (DataIntegrityViolationException e) {
            // 동시에 같은 유저의 카운터가 먼저 생성된 경우: 실제 카운터를 재조회해 다시 반영한다
            counter = userVisitCounterRepository.findByUserId(userId).orElseThrow(() -> e);
            counter.increase();
            userVisitCounterRepository.save(counter);
        }
        return counter.getVisitCount();
    }

    private void awardBadge(Long userId, Long badgeId) {
        userBadgeRepository.save(UserBadge.create(userId, badgeId, LocalDateTime.now()));
    }
}

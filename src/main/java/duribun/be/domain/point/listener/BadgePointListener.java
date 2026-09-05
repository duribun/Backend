package duribun.be.domain.point.listener;

import duribun.be.domain.badge.event.BadgeAcquiredEvent;
import duribun.be.domain.point.service.PointReason;
import duribun.be.domain.point.service.PointService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class BadgePointListener {

    private static final int BADGE_ACQUIRED_REWARD = 50;

    private final PointService pointService;

    public BadgePointListener(PointService pointService) {
        this.pointService = pointService;
    }

    @EventListener
    public void handleBadgeAcquired(BadgeAcquiredEvent event) {
        pointService.earn(event.userId(), BADGE_ACQUIRED_REWARD, PointReason.BADGE_ACQUIRED);
    }
}

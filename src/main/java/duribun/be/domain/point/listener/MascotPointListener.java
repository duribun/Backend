package duribun.be.domain.point.listener;

import duribun.be.domain.mascot.event.MascotAcquiredEvent;
import duribun.be.domain.point.service.PointReason;
import duribun.be.domain.point.service.PointService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MascotPointListener {

    private static final int MASCOT_ACQUIRED_REWARD = 20;

    private final PointService pointService;

    public MascotPointListener(PointService pointService) {
        this.pointService = pointService;
    }

    @EventListener
    public void handleMascotAcquired(MascotAcquiredEvent event) {
        pointService.earn(event.userId(), MASCOT_ACQUIRED_REWARD, PointReason.MASCOT_COLLECT);
    }
}

package duribun.be.domain.point.listener;

import duribun.be.domain.badge.event.BadgeAcquiredEvent;
import duribun.be.domain.point.service.PointReason;
import duribun.be.domain.point.service.PointService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BadgePointListenerTest {

    @Mock
    private PointService pointService;

    @Test
    void handleBadgeAcquired_칭호_획득_이벤트를_받으면_50포인트_적립을_요청한다() {
        BadgePointListener listener = new BadgePointListener(pointService);

        listener.handleBadgeAcquired(new BadgeAcquiredEvent(1L, 3L, "SEEDLING"));

        verify(pointService).earn(1L, 50, PointReason.BADGE_ACQUIRED);
    }
}

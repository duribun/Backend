package duribun.be.domain.point.listener;

import duribun.be.domain.mascot.event.MascotAcquiredEvent;
import duribun.be.domain.point.service.PointReason;
import duribun.be.domain.point.service.PointService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MascotPointListenerTest {

    @Mock
    private PointService pointService;

    @Test
    void handleMascotAcquired_마스코트_획득_이벤트를_받으면_20포인트_적립을_요청한다() {
        MascotPointListener listener = new MascotPointListener(pointService);

        listener.handleMascotAcquired(new MascotAcquiredEvent(1L, 10L, 5L));

        verify(pointService).earn(1L, 20, PointReason.MASCOT_COLLECT);
    }
}

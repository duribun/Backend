package duribun.be.domain.location.event;

import java.util.ArrayList;
import java.util.List;

public record LocationVerifiedEvent(
        Long userId,
        Long regionId,
        boolean isFirstVisit,
        List<NewlyAcquiredMascotInfo> newlyAcquiredMascots
) {

    public LocationVerifiedEvent(Long userId, Long regionId, boolean isFirstVisit) {
        this(userId, regionId, isFirstVisit, new ArrayList<>());
    }

    /**
     * 이 이벤트를 처리하는 동안(같은 트랜잭션 내에서) 새로 지급된 마스코트가 있다면
     * 구독자(mascot 도메인)가 이 목록에 채워 넣는다. location 도메인은 mascot의
     * 엔티티/리포지토리를 직접 알지 못하므로, 동기 이벤트 처리 결과를 돌려받는 용도로만 쓴다.
     */
    public void addNewlyAcquiredMascot(NewlyAcquiredMascotInfo info) {
        newlyAcquiredMascots.add(info);
    }
}

package duribun.be.domain.location.event;

/**
 * {@link LocationVerifiedEvent} 처리 중 새로 지급된 마스코트 정보.
 * location 도메인이 소유하는 결과 전달용 타입이며, mascot 도메인의 엔티티를 노출하지 않는다.
 */
public record NewlyAcquiredMascotInfo(
        Long mascotId,
        String name,
        String imageUrl
) {
}

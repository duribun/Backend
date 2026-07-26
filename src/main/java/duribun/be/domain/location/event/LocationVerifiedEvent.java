package duribun.be.domain.location.event;

public record LocationVerifiedEvent(
        Long userId,
        Long regionId,
        boolean isFirstVisit
) {
}

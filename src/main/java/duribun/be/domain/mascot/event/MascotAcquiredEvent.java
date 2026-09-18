package duribun.be.domain.mascot.event;

public record MascotAcquiredEvent(
        Long userId,
        Long mascotId,
        Long regionId
) {
}

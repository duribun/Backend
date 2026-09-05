package duribun.be.domain.badge.event;

public record BadgeAcquiredEvent(
        Long userId,
        Long badgeId,
        String badgeCode
) {
}

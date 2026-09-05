package duribun.be.domain.badge.dto;

import duribun.be.domain.badge.entity.Badge;
import duribun.be.domain.badge.entity.UserBadge;

import java.time.LocalDateTime;

public record BadgeResponse(
        String code,
        String name,
        String description,
        Integer requiredMascotCount,
        String iconUrl,
        boolean acquired,
        LocalDateTime acquiredAt
) {
    public static BadgeResponse of(Badge badge, UserBadge userBadge) {
        boolean acquired = userBadge != null;
        return new BadgeResponse(
                badge.getCode(),
                badge.getName(),
                badge.getDescription(),
                badge.getRequiredMascotCount(),
                badge.getIconUrl(),
                acquired,
                acquired ? userBadge.getAcquiredAt() : null
        );
    }
}

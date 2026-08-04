package duribun.be.domain.badge.dto;

import duribun.be.domain.badge.entity.Badge;
import duribun.be.domain.badge.entity.UserBadge;

import java.time.LocalDateTime;

public record MyBadgeResponse(
        String code,
        String name,
        String description,
        String iconUrl,
        LocalDateTime acquiredAt
) {
    public static MyBadgeResponse of(Badge badge, UserBadge userBadge) {
        return new MyBadgeResponse(
                badge.getCode(),
                badge.getName(),
                badge.getDescription(),
                badge.getIconUrl(),
                userBadge.getAcquiredAt()
        );
    }
}

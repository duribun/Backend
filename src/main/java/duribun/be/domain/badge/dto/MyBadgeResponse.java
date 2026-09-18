package duribun.be.domain.badge.dto;

import duribun.be.domain.badge.entity.Badge;
import duribun.be.domain.badge.entity.UserBadge;

import java.time.LocalDateTime;

/**
 * requiredMascotCount(등급 기준값)를 포함한다. "현재 칭호"(가장 높은 단계)를 판별하려면
 * 이 값이 필요한데, 여러 칭호를 한 번에 획득한 경우 acquiredAt만으로는 최고 단계를 가려낼 수
 * 없기 때문이다(BadgeService#getMyBadges에서 이 값 기준 내림차순으로 정렬해 내려준다).
 */
public record MyBadgeResponse(
        String code,
        String name,
        String description,
        String iconUrl,
        Integer requiredMascotCount,
        LocalDateTime acquiredAt
) {
    public static MyBadgeResponse of(Badge badge, UserBadge userBadge) {
        return new MyBadgeResponse(
                badge.getCode(),
                badge.getName(),
                badge.getDescription(),
                badge.getIconUrl(),
                badge.getRequiredMascotCount(),
                userBadge.getAcquiredAt()
        );
    }
}

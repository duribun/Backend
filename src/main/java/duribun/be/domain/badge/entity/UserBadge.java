package duribun.be.domain.badge.entity;

import duribun.be.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_badges", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "badge_id"}))
public class UserBadge extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "badge_id", nullable = false)
    private Long badgeId;

    @Column(name = "acquired_at", nullable = false)
    private LocalDateTime acquiredAt;

    private UserBadge(Long userId, Long badgeId, LocalDateTime acquiredAt) {
        this.userId = userId;
        this.badgeId = badgeId;
        this.acquiredAt = acquiredAt;
    }

    public static UserBadge create(Long userId, Long badgeId, LocalDateTime acquiredAt) {
        return new UserBadge(userId, badgeId, acquiredAt);
    }
}

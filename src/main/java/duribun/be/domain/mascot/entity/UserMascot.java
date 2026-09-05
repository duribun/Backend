package duribun.be.domain.mascot.entity;

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
@Table(name = "user_mascots", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "mascot_id"}))
public class UserMascot extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "mascot_id", nullable = false)
    private Long mascotId;

    @Column(name = "acquired_at", nullable = false)
    private LocalDateTime acquiredAt;

    private UserMascot(Long userId, Long mascotId, LocalDateTime acquiredAt) {
        this.userId = userId;
        this.mascotId = mascotId;
        this.acquiredAt = acquiredAt;
    }

    public static UserMascot create(Long userId, Long mascotId, LocalDateTime acquiredAt) {
        return new UserMascot(userId, mascotId, acquiredAt);
    }
}

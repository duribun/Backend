package duribun.be.domain.character.entity;

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
@Table(name = "user_characters", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "character_id"}))
public class UserCharacter extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "character_id", nullable = false)
    private Long characterId;

    @Column(name = "acquired_at", nullable = false)
    private LocalDateTime acquiredAt;

    private UserCharacter(Long userId, Long characterId, LocalDateTime acquiredAt) {
        this.userId = userId;
        this.characterId = characterId;
        this.acquiredAt = acquiredAt;
    }

    public static UserCharacter create(Long userId, Long characterId, LocalDateTime acquiredAt) {
        return new UserCharacter(userId, characterId, acquiredAt);
    }
}

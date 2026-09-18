package duribun.be.domain.badge.entity;

import duribun.be.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_mascot_counters", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
public class UserMascotCounter extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "mascot_count", nullable = false)
    private Integer mascotCount;

    @Version
    private Long version;

    private UserMascotCounter(Long userId) {
        this.userId = userId;
        this.mascotCount = 0;
    }

    public static UserMascotCounter create(Long userId) {
        return new UserMascotCounter(userId);
    }

    public void increase() {
        this.mascotCount += 1;
    }
}

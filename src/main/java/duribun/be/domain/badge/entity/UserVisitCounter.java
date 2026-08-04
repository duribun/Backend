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
@Table(name = "user_visit_counters", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
public class UserVisitCounter extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "visit_count", nullable = false)
    private Integer visitCount;

    @Version
    private Long version;

    private UserVisitCounter(Long userId) {
        this.userId = userId;
        this.visitCount = 0;
    }

    public static UserVisitCounter create(Long userId) {
        return new UserVisitCounter(userId);
    }

    public void increase() {
        this.visitCount += 1;
    }
}

package duribun.be.domain.point.entity;

import duribun.be.common.entity.BaseTimeEntity;
import duribun.be.domain.point.service.PointReason;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "point_histories")
public class PointHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointReason reason;

    @Column(nullable = false)
    private Integer balanceAfter;

    private PointHistory(Long userId, Integer amount, PointReason reason, Integer balanceAfter) {
        this.userId = userId;
        this.amount = amount;
        this.reason = reason;
        this.balanceAfter = balanceAfter;
    }

    public static PointHistory create(Long userId, Integer amount, PointReason reason, Integer balanceAfter) {
        return new PointHistory(userId, amount, reason, balanceAfter);
    }
}

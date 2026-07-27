package duribun.be.domain.point.entity;

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
@Table(name = "point_accounts", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
public class PointAccount extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer balance;

    @Version
    private Long version;

    private PointAccount(Long userId) {
        this.userId = userId;
        this.balance = 0;
    }

    public static PointAccount create(Long userId) {
        return new PointAccount(userId);
    }

    public void increaseBalance(int amount) {
        this.balance += amount;
    }

    public void decreaseBalance(int amount) {
        if (amount > this.balance) {
            throw new IllegalStateException("잔액보다 많은 금액을 차감할 수 없습니다");
        }
        this.balance -= amount;
    }
}

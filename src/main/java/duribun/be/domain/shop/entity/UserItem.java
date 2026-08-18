package duribun.be.domain.shop.entity;

import duribun.be.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "user_items", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "item_id"}))
public class UserItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemCategory category;

    @Column(name = "is_equipped", nullable = false)
    private boolean isEquipped = false;

    @Column(name = "purchased_at", nullable = false)
    private LocalDateTime purchasedAt;

    private UserItem(Long userId, Long itemId, ItemCategory category, LocalDateTime purchasedAt) {
        this.userId = userId;
        this.itemId = itemId;
        this.category = category;
        this.purchasedAt = purchasedAt;
    }

    public static UserItem create(Long userId, Long itemId, ItemCategory category, LocalDateTime purchasedAt) {
        return new UserItem(userId, itemId, category, purchasedAt);
    }

    public void equip() {
        this.isEquipped = true;
    }

    public void unequip() {
        this.isEquipped = false;
    }
}

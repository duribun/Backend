package duribun.be.domain.shop.dto;

import duribun.be.domain.shop.entity.Item;
import duribun.be.domain.shop.entity.ItemCategory;
import duribun.be.domain.shop.entity.UserItem;
import java.time.LocalDateTime;

public record MyItemResponse(
        Long id,
        String name,
        String imageUrl,
        ItemCategory category,
        boolean isEquipped,
        LocalDateTime purchasedAt
) {
    public static MyItemResponse of(Item item, UserItem userItem) {
        return new MyItemResponse(
                item.getId(),
                item.getName(),
                item.getImageUrl(),
                item.getCategory(),
                userItem.isEquipped(),
                userItem.getPurchasedAt()
        );
    }
}

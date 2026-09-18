package duribun.be.domain.shop.dto;

import duribun.be.domain.shop.entity.Item;
import duribun.be.domain.shop.entity.ItemCategory;

public record ItemResponse(
        Long id,
        String name,
        String description,
        int price,
        String imageUrl,
        ItemCategory category
) {
    public static ItemResponse from(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.getImageUrl(),
                item.getCategory()
        );
    }
}

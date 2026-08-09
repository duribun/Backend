package duribun.be.domain.shop.dto;

public record PurchaseResponse(
        Long itemId,
        String itemName,
        int remainingPoints
) {
    public static PurchaseResponse of(Long itemId, String itemName, int remainingPoints) {
        return new PurchaseResponse(itemId, itemName, remainingPoints);
    }
}

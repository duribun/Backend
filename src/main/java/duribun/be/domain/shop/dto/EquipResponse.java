package duribun.be.domain.shop.dto;

public record EquipResponse(
        Long itemId,
        boolean isEquipped
) {
    public static EquipResponse of(Long itemId, boolean isEquipped) {
        return new EquipResponse(itemId, isEquipped);
    }
}

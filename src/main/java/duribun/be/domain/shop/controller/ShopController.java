package duribun.be.domain.shop.controller;

import duribun.be.domain.shop.dto.EquipResponse;
import duribun.be.domain.shop.dto.ItemResponse;
import duribun.be.domain.shop.dto.MyItemResponse;
import duribun.be.domain.shop.dto.PurchaseResponse;
import duribun.be.domain.shop.service.ShopService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/shop")
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @GetMapping("/items")
    public ResponseEntity<List<ItemResponse>> getItems() {
        return ResponseEntity.ok(shopService.getAllItems());
    }

    @GetMapping("/items/me")
    public ResponseEntity<List<MyItemResponse>> getMyItems(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(shopService.getMyItems(userId));
    }

    @PostMapping("/items/{itemId}/purchase")
    public ResponseEntity<PurchaseResponse> purchase(@AuthenticationPrincipal Long userId,
                                                      @PathVariable Long itemId) {
        return ResponseEntity.ok(shopService.purchaseItem(userId, itemId));
    }

    @PatchMapping("/items/{itemId}/equip")
    public ResponseEntity<EquipResponse> equip(@AuthenticationPrincipal Long userId,
                                                @PathVariable Long itemId) {
        return ResponseEntity.ok(shopService.toggleEquip(userId, itemId));
    }
}

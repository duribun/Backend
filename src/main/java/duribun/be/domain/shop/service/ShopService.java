package duribun.be.domain.shop.service;

import duribun.be.common.TimeProvider;
import duribun.be.domain.point.service.PointReason;
import duribun.be.domain.point.service.PointService;
import duribun.be.domain.shop.dto.EquipResponse;
import duribun.be.domain.shop.dto.ItemResponse;
import duribun.be.domain.shop.dto.MyItemResponse;
import duribun.be.domain.shop.dto.PurchaseResponse;
import duribun.be.domain.shop.entity.Item;
import duribun.be.domain.shop.entity.UserItem;
import duribun.be.domain.shop.repository.ItemRepository;
import duribun.be.domain.shop.repository.UserItemRepository;
import duribun.be.global.exception.AlreadyPurchasedException;
import duribun.be.global.exception.ItemNotFoundException;
import duribun.be.global.exception.ItemNotOwnedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShopService {

    private final ItemRepository itemRepository;
    private final UserItemRepository userItemRepository;
    private final PointService pointService;
    private final TimeProvider timeProvider;

    public ShopService(ItemRepository itemRepository, UserItemRepository userItemRepository,
                       PointService pointService, TimeProvider timeProvider) {
        this.itemRepository = itemRepository;
        this.userItemRepository = userItemRepository;
        this.pointService = pointService;
        this.timeProvider = timeProvider;
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> getAllItems() {
        return itemRepository.findAll().stream()
                .map(ItemResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MyItemResponse> getMyItems(Long userId) {
        List<UserItem> userItems = userItemRepository.findByUserId(userId);
        Map<Long, Item> itemsById = itemRepository
                .findAllById(userItems.stream().map(UserItem::getItemId).toList())
                .stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));

        return userItems.stream()
                .map(userItem -> MyItemResponse.of(itemsById.get(userItem.getItemId()), userItem))
                .toList();
    }

    public PurchaseResponse purchaseItem(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("존재하지 않는 아이템입니다."));

        if (userItemRepository.existsByUserIdAndItemId(userId, itemId)) {
            throw new AlreadyPurchasedException("이미 구매한 아이템입니다.");
        }

        pointService.spend(userId, item.getPrice(), PointReason.SHOP_PURCHASE);
        try {
            userItemRepository.save(UserItem.create(userId, itemId, timeProvider.now()));
        } catch (DataIntegrityViolationException e) {
            // 동시에 같은 아이템을 중복 구매 요청한 경우: unique 제약 위반을 논리적 중복 구매로 변환한다
            throw new AlreadyPurchasedException("이미 구매한 아이템입니다.");
        }

        return PurchaseResponse.of(itemId, item.getName(), pointService.getBalance(userId));
    }

    public EquipResponse toggleEquip(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("존재하지 않는 아이템입니다."));
        UserItem userItem = userItemRepository.findByUserIdAndItemId(userId, itemId)
                .orElseThrow(() -> new ItemNotOwnedException("구매하지 않은 아이템입니다."));

        if (!userItem.isEquipped()) {
            unequipSameCategory(userId, item, itemId);
            userItem.equip();
        } else {
            userItem.unequip();
        }

        return EquipResponse.of(itemId, userItem.isEquipped());
    }

    private void unequipSameCategory(Long userId, Item targetItem, Long excludeItemId) {
        List<UserItem> equippedOthers = userItemRepository.findByUserIdAndIsEquippedTrue(userId).stream()
                .filter(ui -> !ui.getItemId().equals(excludeItemId))
                .toList();

        if (equippedOthers.isEmpty()) {
            return;
        }

        List<Long> otherItemIds = equippedOthers.stream().map(UserItem::getItemId).toList();
        Map<Long, Item> otherItems = itemRepository.findAllById(otherItemIds).stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));

        equippedOthers.stream()
                .filter(ui -> {
                    Item other = otherItems.get(ui.getItemId());
                    return other != null && other.getCategory() == targetItem.getCategory();
                })
                .forEach(UserItem::unequip);
    }
}

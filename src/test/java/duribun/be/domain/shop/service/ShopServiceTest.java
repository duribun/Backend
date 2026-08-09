package duribun.be.domain.shop.service;

import duribun.be.common.FixedTimeProvider;
import duribun.be.common.TestEntityUtils;
import duribun.be.domain.point.service.PointReason;
import duribun.be.domain.point.service.PointService;
import duribun.be.domain.shop.dto.EquipResponse;
import duribun.be.domain.shop.dto.ItemResponse;
import duribun.be.domain.shop.dto.MyItemResponse;
import duribun.be.domain.shop.dto.PurchaseResponse;
import duribun.be.domain.shop.entity.Item;
import duribun.be.domain.shop.entity.ItemCategory;
import duribun.be.domain.shop.entity.UserItem;
import duribun.be.domain.shop.repository.ItemRepository;
import duribun.be.domain.shop.repository.UserItemRepository;
import duribun.be.global.exception.AlreadyPurchasedException;
import duribun.be.global.exception.InsufficientPointException;
import duribun.be.global.exception.ItemNotFoundException;
import duribun.be.global.exception.ItemNotOwnedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    @Mock private ItemRepository itemRepository;
    @Mock private UserItemRepository userItemRepository;
    @Mock private PointService pointService;

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 8, 9, 12, 0);

    private ShopService shopService;

    @BeforeEach
    void setUp() {
        shopService = new ShopService(
                itemRepository, userItemRepository, pointService,
                new FixedTimeProvider(FIXED_NOW)
        );
    }

    private Item itemWithId(Long id, ItemCategory category) {
        Item item = Item.create("아이템" + id, "설명", 500, null, category);
        TestEntityUtils.setId(item, id);
        return item;
    }

    private UserItem userItemWithId(Long id, Long userId, Long itemId) {
        UserItem ui = UserItem.create(userId, itemId, FIXED_NOW);
        TestEntityUtils.setId(ui, id);
        return ui;
    }

    // ── 전체 아이템 조회 ──────────────────────────────────────────────────────

    @Nested
    class 전체_아이템_조회 {

        @Test
        void 전체_아이템_목록을_반환한다() {
            Item item1 = itemWithId(1L, ItemCategory.GLASSES);
            Item item2 = itemWithId(2L, ItemCategory.BAG);
            when(itemRepository.findAll()).thenReturn(List.of(item1, item2));

            List<ItemResponse> result = shopService.getAllItems();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).id()).isEqualTo(1L);
            assertThat(result.get(1).id()).isEqualTo(2L);
        }

        @Test
        void 아이템이_없으면_빈_목록을_반환한다() {
            when(itemRepository.findAll()).thenReturn(List.of());

            assertThat(shopService.getAllItems()).isEmpty();
        }
    }

    // ── 내 아이템 조회 ────────────────────────────────────────────────────────

    @Nested
    class 내_아이템_조회 {

        @Test
        void 구매한_아이템의_착용여부를_포함해_반환한다() {
            Item item = itemWithId(1L, ItemCategory.GLASSES);
            UserItem userItem = userItemWithId(10L, 1L, 1L);
            userItem.equip();
            when(userItemRepository.findByUserId(1L)).thenReturn(List.of(userItem));
            when(itemRepository.findAllById(List.of(1L))).thenReturn(List.of(item));

            List<MyItemResponse> result = shopService.getMyItems(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(1L);
            assertThat(result.get(0).isEquipped()).isTrue();
        }

        @Test
        void 구매한_아이템이_없으면_빈_목록을_반환한다() {
            when(userItemRepository.findByUserId(1L)).thenReturn(List.of());
            when(itemRepository.findAllById(List.of())).thenReturn(List.of());

            assertThat(shopService.getMyItems(1L)).isEmpty();
        }
    }

    // ── 아이템 구매 ───────────────────────────────────────────────────────────

    @Nested
    class 아이템_구매 {

        @Test
        void 구매시_포인트가_차감되고_UserItem이_저장된다() {
            Item item = itemWithId(1L, ItemCategory.GLASSES);
            when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(userItemRepository.existsByUserIdAndItemId(1L, 1L)).thenReturn(false);
            when(pointService.getBalance(1L)).thenReturn(500);

            shopService.purchaseItem(1L, 1L);

            verify(pointService).spend(1L, 500, PointReason.SHOP_PURCHASE);
            verify(userItemRepository).save(any(UserItem.class));
        }

        @Test
        void 구매시_purchasedAt이_TimeProvider의_현재시간으로_설정된다() {
            Item item = itemWithId(1L, ItemCategory.GLASSES);
            when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(userItemRepository.existsByUserIdAndItemId(1L, 1L)).thenReturn(false);
            when(pointService.getBalance(1L)).thenReturn(500);

            shopService.purchaseItem(1L, 1L);

            ArgumentCaptor<UserItem> captor = ArgumentCaptor.forClass(UserItem.class);
            verify(userItemRepository).save(captor.capture());
            assertThat(captor.getValue().getPurchasedAt()).isEqualTo(FIXED_NOW);
        }

        @Test
        void 구매_성공시_아이템id와_잔여포인트를_응답에_포함한다() {
            Item item = itemWithId(1L, ItemCategory.GLASSES);
            when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(userItemRepository.existsByUserIdAndItemId(1L, 1L)).thenReturn(false);
            when(pointService.getBalance(1L)).thenReturn(700);

            PurchaseResponse response = shopService.purchaseItem(1L, 1L);

            assertThat(response.itemId()).isEqualTo(1L);
            assertThat(response.remainingPoints()).isEqualTo(700);
        }

        @Test
        void 존재하지_않는_아이템_구매시_ItemNotFoundException을_던진다() {
            when(itemRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> shopService.purchaseItem(1L, 99L))
                    .isInstanceOf(ItemNotFoundException.class)
                    .hasMessage("존재하지 않는 아이템입니다.");

            verify(userItemRepository, never()).save(any());
        }

        @Test
        void 이미_구매한_아이템_재구매시_AlreadyPurchasedException을_던진다() {
            Item item = itemWithId(1L, ItemCategory.BAG);
            when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(userItemRepository.existsByUserIdAndItemId(1L, 1L)).thenReturn(true);

            assertThatThrownBy(() -> shopService.purchaseItem(1L, 1L))
                    .isInstanceOf(AlreadyPurchasedException.class)
                    .hasMessage("이미 구매한 아이템입니다.");

            verify(pointService, never()).spend(anyLong(), anyInt(), any(PointReason.class));
            verify(userItemRepository, never()).save(any());
        }

        @Test
        void 포인트_부족시_UserItem을_저장하지_않는다() {
            Item item = itemWithId(1L, ItemCategory.CARRIER);
            when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(userItemRepository.existsByUserIdAndItemId(1L, 1L)).thenReturn(false);
            doThrow(new InsufficientPointException("포인트 잔액이 부족합니다."))
                    .when(pointService).spend(1L, 500, PointReason.SHOP_PURCHASE);

            assertThatThrownBy(() -> shopService.purchaseItem(1L, 1L))
                    .isInstanceOf(InsufficientPointException.class)
                    .hasMessage("포인트 잔액이 부족합니다.");

            verify(userItemRepository, never()).save(any());
        }
    }

    // ── 착용 토글 ─────────────────────────────────────────────────────────────

    @Nested
    class 착용_토글 {

        @Test
        void 미착용_아이템을_착용하면_isEquipped가_true가_된다() {
            Item item = itemWithId(1L, ItemCategory.GLASSES);
            UserItem userItem = userItemWithId(10L, 1L, 1L);
            when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(userItemRepository.findByUserIdAndItemId(1L, 1L)).thenReturn(Optional.of(userItem));
            when(userItemRepository.findByUserIdAndIsEquippedTrue(1L)).thenReturn(List.of());

            EquipResponse response = shopService.toggleEquip(1L, 1L);

            assertThat(response.isEquipped()).isTrue();
            assertThat(userItem.isEquipped()).isTrue();
        }

        @Test
        void 착용중인_아이템을_다시_누르면_isEquipped가_false가_된다() {
            Item item = itemWithId(1L, ItemCategory.HAT);
            UserItem userItem = userItemWithId(10L, 1L, 1L);
            userItem.equip();
            when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(userItemRepository.findByUserIdAndItemId(1L, 1L)).thenReturn(Optional.of(userItem));

            EquipResponse response = shopService.toggleEquip(1L, 1L);

            assertThat(response.isEquipped()).isFalse();
            assertThat(userItem.isEquipped()).isFalse();
        }

        @ParameterizedTest(name = "{0} 카테고리 - 같은 카테고리 기존 착용 아이템이 자동으로 해제된다")
        @EnumSource(ItemCategory.class)
        void 같은_카테고리_착용_중인_아이템이_자동으로_해제된다(ItemCategory category) {
            Item item1 = itemWithId(1L, category);
            Item item2 = itemWithId(2L, category);
            UserItem userItem1 = userItemWithId(10L, 1L, 1L);
            userItem1.equip();
            UserItem userItem2 = userItemWithId(20L, 1L, 2L);

            when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));
            when(userItemRepository.findByUserIdAndItemId(1L, 2L)).thenReturn(Optional.of(userItem2));
            when(userItemRepository.findByUserIdAndIsEquippedTrue(1L)).thenReturn(List.of(userItem1));
            when(itemRepository.findAllById(List.of(1L))).thenReturn(List.of(item1));

            EquipResponse response = shopService.toggleEquip(1L, 2L);

            assertThat(response.isEquipped()).isTrue();
            assertThat(userItem1.isEquipped()).isFalse();
            assertThat(userItem2.isEquipped()).isTrue();
        }

        @Test
        void 다른_카테고리_착용중인_아이템은_그대로_유지된다() {
            Item glasses = itemWithId(1L, ItemCategory.GLASSES);
            Item hat = itemWithId(2L, ItemCategory.HAT);
            UserItem equippedGlasses = userItemWithId(10L, 1L, 1L);
            equippedGlasses.equip();
            UserItem newHat = userItemWithId(20L, 1L, 2L);

            when(itemRepository.findById(2L)).thenReturn(Optional.of(hat));
            when(userItemRepository.findByUserIdAndItemId(1L, 2L)).thenReturn(Optional.of(newHat));
            when(userItemRepository.findByUserIdAndIsEquippedTrue(1L)).thenReturn(List.of(equippedGlasses));
            when(itemRepository.findAllById(List.of(1L))).thenReturn(List.of(glasses));

            shopService.toggleEquip(1L, 2L);

            assertThat(equippedGlasses.isEquipped()).isTrue();
            assertThat(newHat.isEquipped()).isTrue();
        }

        @Test
        void 존재하지_않는_아이템이면_ItemNotFoundException을_던진다() {
            when(itemRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> shopService.toggleEquip(1L, 99L))
                    .isInstanceOf(ItemNotFoundException.class)
                    .hasMessage("존재하지 않는 아이템입니다.");
        }

        @Test
        void 미구매_아이템이면_ItemNotOwnedException을_던진다() {
            Item item = itemWithId(1L, ItemCategory.BAG);
            when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(userItemRepository.findByUserIdAndItemId(1L, 1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> shopService.toggleEquip(1L, 1L))
                    .isInstanceOf(ItemNotOwnedException.class)
                    .hasMessage("구매하지 않은 아이템입니다.");
        }
    }
}

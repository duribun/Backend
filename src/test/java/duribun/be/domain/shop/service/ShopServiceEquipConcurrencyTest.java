package duribun.be.domain.shop.service;

import duribun.be.domain.shop.entity.Item;
import duribun.be.domain.shop.entity.ItemCategory;
import duribun.be.domain.shop.entity.UserItem;
import duribun.be.domain.shop.repository.ItemRepository;
import duribun.be.domain.shop.repository.UserItemRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ShopServiceEquipConcurrencyTest {

    @Autowired private ShopService shopService;
    @Autowired private ItemRepository itemRepository;
    @Autowired private UserItemRepository userItemRepository;

    @AfterEach
    void tearDown() {
        userItemRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    void 같은_카테고리의_서로_다른_아이템을_동시에_착용해도_하나만_착용_상태로_남는다() throws InterruptedException {
        Long userId = 1L;
        Item item1 = itemRepository.saveAndFlush(Item.create("선글라스1", "설명", 500, null, ItemCategory.GLASSES));
        Item item2 = itemRepository.saveAndFlush(Item.create("선글라스2", "설명", 500, null, ItemCategory.GLASSES));
        userItemRepository.saveAndFlush(UserItem.create(userId, item1.getId(), item1.getCategory(), LocalDateTime.now()));
        userItemRepository.saveAndFlush(UserItem.create(userId, item2.getId(), item2.getCategory(), LocalDateTime.now()));

        List<Long> itemIds = List.of(item1.getId(), item2.getId());
        int threadCount = itemIds.size();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (Long itemId : itemIds) {
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    shopService.toggleEquip(userId, itemId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        boolean finished = doneLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(finished).isTrue();
        List<UserItem> myItems = userItemRepository.findByUserId(userId);
        assertThat(myItems).filteredOn(UserItem::isEquipped).hasSize(1);
    }
}

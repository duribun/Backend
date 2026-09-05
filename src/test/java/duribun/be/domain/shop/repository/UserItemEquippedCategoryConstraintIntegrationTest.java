package duribun.be.domain.shop.repository;

import duribun.be.domain.shop.entity.Item;
import duribun.be.domain.shop.entity.ItemCategory;
import duribun.be.domain.shop.entity.UserItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * user_items에 category를 반정규화하고 ddl-auto: update가 만들 수 없는 부분 유니크
 * 인덱스(같은 유저의 같은 카테고리는 최대 1개만 is_equipped=true)를 수동으로 추가하는
 * docs/sql/user_items_equip_category_constraint.sql이 실제 Postgres에서 의도대로
 * 동작하는지 검증한다. ShopService의 비관적 락을 우회해 리포지토리를 직접 호출함으로써,
 * 락을 거치지 않는 미래의 배치/어드민 경로에서도 DB가 불변식을 지키는지 확인한다.
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("postgres-it")
class UserItemEquippedCategoryConstraintIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    @Autowired private ItemRepository itemRepository;
    @Autowired private UserItemRepository userItemRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void createPartialUniqueIndex() {
        // docs/sql/user_items_equip_category_constraint.sql의 마지막 단계와 동일한 인덱스.
        // ddl-auto가 만든 나머지 스키마 위에, JPA로는 표현할 수 없는 부분 유니크 인덱스만 수동으로 얹는다.
        jdbcTemplate.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS uq_user_items_equipped_category
                ON user_items (user_id, category)
                WHERE is_equipped = true
                """);
    }

    @Test
    void 서비스를_거치지_않고_직접_저장해도_같은_카테고리를_두_개_이상_착용_상태로_둘_수_없다() {
        Long userId = 1L;
        Item item1 = itemRepository.saveAndFlush(Item.create("선글라스1", "설명", 500, null, ItemCategory.GLASSES));
        Item item2 = itemRepository.saveAndFlush(Item.create("선글라스2", "설명", 500, null, ItemCategory.GLASSES));

        UserItem userItem1 = UserItem.create(userId, item1.getId(), item1.getCategory(), LocalDateTime.now());
        userItem1.equip();
        userItemRepository.saveAndFlush(userItem1);

        UserItem userItem2 = UserItem.create(userId, item2.getId(), item2.getCategory(), LocalDateTime.now());
        userItem2.equip();

        assertThatThrownBy(() -> userItemRepository.saveAndFlush(userItem2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

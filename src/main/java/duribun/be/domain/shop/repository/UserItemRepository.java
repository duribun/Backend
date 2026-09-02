package duribun.be.domain.shop.repository;

import duribun.be.domain.shop.entity.ItemCategory;
import duribun.be.domain.shop.entity.UserItem;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserItemRepository extends JpaRepository<UserItem, Long> {

    List<UserItem> findByUserId(Long userId);

    boolean existsByUserIdAndItemId(Long userId, Long itemId);

    // 착용 토글 시 같은 유저의 같은 카테고리 아이템 행만 잠가 동시 요청을 직렬화한다 (같은 카테고리 동시 착용 레이스 방지).
    // 다른 카테고리끼리는 서로 잠그지 않아 동시 착용/해제가 불필요하게 직렬화되지 않는다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ui from UserItem ui where ui.userId = :userId and ui.category = :category")
    List<UserItem> findByUserIdAndCategoryForUpdate(@Param("userId") Long userId, @Param("category") ItemCategory category);
}

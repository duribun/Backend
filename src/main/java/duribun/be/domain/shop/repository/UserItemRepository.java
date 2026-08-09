package duribun.be.domain.shop.repository;

import duribun.be.domain.shop.entity.UserItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserItemRepository extends JpaRepository<UserItem, Long> {

    List<UserItem> findByUserId(Long userId);

    Optional<UserItem> findByUserIdAndItemId(Long userId, Long itemId);

    boolean existsByUserIdAndItemId(Long userId, Long itemId);

    List<UserItem> findByUserIdAndIsEquippedTrue(Long userId);
}

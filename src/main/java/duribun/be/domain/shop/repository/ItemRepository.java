package duribun.be.domain.shop.repository;

import duribun.be.domain.shop.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}

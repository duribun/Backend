package duribun.be.domain.badge.repository;

import duribun.be.domain.badge.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    boolean existsByUserIdAndBadgeId(Long userId, Long badgeId);

    List<UserBadge> findByUserId(Long userId);
}

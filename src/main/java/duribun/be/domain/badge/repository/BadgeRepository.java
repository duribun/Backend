package duribun.be.domain.badge.repository;

import duribun.be.domain.badge.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    List<Badge> findByRequiredVisitCountLessThanEqual(Integer visitCount);
}

package duribun.be.domain.badge.repository;

import duribun.be.domain.badge.entity.UserVisitCounter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserVisitCounterRepository extends JpaRepository<UserVisitCounter, Long> {

    Optional<UserVisitCounter> findByUserId(Long userId);
}

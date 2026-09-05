package duribun.be.domain.badge.repository;

import duribun.be.domain.badge.entity.UserMascotCounter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserMascotCounterRepository extends JpaRepository<UserMascotCounter, Long> {

    Optional<UserMascotCounter> findByUserId(Long userId);
}

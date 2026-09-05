package duribun.be.domain.mascot.repository;

import duribun.be.domain.mascot.entity.UserMascot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserMascotRepository extends JpaRepository<UserMascot, Long> {

    boolean existsByUserIdAndMascotId(Long userId, Long mascotId);

    List<UserMascot> findByUserId(Long userId);
}

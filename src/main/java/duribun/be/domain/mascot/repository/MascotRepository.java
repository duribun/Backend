package duribun.be.domain.mascot.repository;

import duribun.be.domain.mascot.entity.Mascot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MascotRepository extends JpaRepository<Mascot, Long> {

    Optional<Mascot> findByRegionId(Long regionId);
}

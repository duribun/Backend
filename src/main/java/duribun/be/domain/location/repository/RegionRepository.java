package duribun.be.domain.location.repository;

import duribun.be.domain.location.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {

    Optional<Region> findBySigunguCode(String sigunguCode);
}

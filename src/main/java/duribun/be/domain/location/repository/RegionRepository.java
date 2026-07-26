package duribun.be.domain.location.repository;

import duribun.be.domain.location.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {
}

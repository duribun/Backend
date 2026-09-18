package duribun.be.domain.map.repository;

import duribun.be.domain.map.entity.TourApiRegionMapping;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TourApiRegionMappingRepository extends JpaRepository<TourApiRegionMapping, Long> {

    Optional<TourApiRegionMapping> findByRegionId(Long regionId);
}

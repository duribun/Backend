package duribun.be.domain.map.repository;

import duribun.be.domain.map.entity.Attraction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttractionRepository extends JpaRepository<Attraction, Long> {

    Optional<Attraction> findByContentId(String contentId);

    List<Attraction> findByRegionId(Long regionId);
}

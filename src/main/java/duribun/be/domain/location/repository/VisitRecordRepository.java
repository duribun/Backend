package duribun.be.domain.location.repository;

import duribun.be.domain.location.entity.VisitRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VisitRecordRepository extends JpaRepository<VisitRecord, Long> {

    Optional<VisitRecord> findByUserIdAndRegionId(Long userId, Long regionId);

    List<VisitRecord> findByUserId(Long userId);
}

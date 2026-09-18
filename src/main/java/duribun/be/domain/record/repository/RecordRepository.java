package duribun.be.domain.record.repository;

import duribun.be.domain.record.entity.TravelRecord;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordRepository extends JpaRepository<TravelRecord, Long> {

    List<TravelRecord> findByUserIdOrderByVisitedAtAsc(Long userId);

    List<TravelRecord> findByUserIdAndVisitedAtBetweenOrderByVisitedAtAsc(
            Long userId, LocalDate from, LocalDate to);
}

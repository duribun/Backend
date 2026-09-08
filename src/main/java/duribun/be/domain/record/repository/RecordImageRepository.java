package duribun.be.domain.record.repository;

import duribun.be.domain.record.entity.RecordImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecordImageRepository extends JpaRepository<RecordImage, Long> {

    List<RecordImage> findByRecordIdOrderBySortOrderAsc(Long recordId);

    List<RecordImage> findByRecordIdInOrderByRecordIdAscSortOrderAsc(List<Long> recordIds);

    void deleteByRecordId(Long recordId);
}

package duribun.be.domain.record.service;

import duribun.be.domain.record.dto.CreateRecordRequest;
import duribun.be.domain.record.dto.RecordResponse;
import duribun.be.domain.record.dto.RecordSummaryResponse;
import duribun.be.domain.record.dto.UpdateRecordRequest;
import duribun.be.domain.record.entity.TravelRecord;
import duribun.be.domain.record.repository.RecordRepository;
import duribun.be.global.exception.RecordForbiddenException;
import duribun.be.global.exception.RecordNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional
public class RecordService {

    private final RecordRepository recordRepository;

    public RecordService(RecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    public RecordResponse createRecord(Long userId, CreateRecordRequest request) {
        TravelRecord record = TravelRecord.create(
                userId,
                request.title(),
                request.content(),
                request.imageUrl(),
                request.visitedAt(),
                request.placeName()
        );
        return RecordResponse.from(recordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public List<RecordSummaryResponse> getMyRecords(Long userId, Integer year, Integer month) {
        if (year == null) {
            return recordRepository.findByUserIdOrderByVisitedAtAsc(userId).stream()
                    .map(RecordSummaryResponse::from)
                    .toList();
        }
        LocalDate from;
        LocalDate to;
        if (month == null) {
            from = LocalDate.of(year, 1, 1);
            to = LocalDate.of(year, 12, 31);
        } else {
            YearMonth yearMonth = YearMonth.of(year, month);
            from = yearMonth.atDay(1);
            to = yearMonth.atEndOfMonth();
        }
        return recordRepository.findByUserIdAndVisitedAtBetweenOrderByVisitedAtAsc(userId, from, to).stream()
                .map(RecordSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecordResponse getRecord(Long userId, Long recordId) {
        TravelRecord record = findRecord(recordId);
        validateOwner(record, userId);
        return RecordResponse.from(record);
    }

    public RecordResponse updateRecord(Long userId, Long recordId, UpdateRecordRequest request) {
        TravelRecord record = findRecord(recordId);
        validateOwner(record, userId);
        record.update(request.title(), request.content(), request.imageUrl(),
                request.visitedAt(), request.placeName());
        return RecordResponse.from(record);
    }

    public void deleteRecord(Long userId, Long recordId) {
        TravelRecord record = findRecord(recordId);
        validateOwner(record, userId);
        recordRepository.delete(record);
    }

    private TravelRecord findRecord(Long recordId) {
        return recordRepository.findById(recordId)
                .orElseThrow(() -> new RecordNotFoundException("존재하지 않는 기록입니다."));
    }

    private void validateOwner(TravelRecord record, Long userId) {
        if (!record.getUserId().equals(userId)) {
            throw new RecordForbiddenException("본인의 기록만 접근할 수 있습니다.");
        }
    }
}

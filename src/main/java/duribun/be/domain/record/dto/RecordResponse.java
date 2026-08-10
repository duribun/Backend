package duribun.be.domain.record.dto;

import duribun.be.domain.record.entity.TravelRecord;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record RecordResponse(
        Long id,
        String title,
        String content,
        String imageUrl,
        LocalDate visitedAt,
        String placeName,
        LocalDateTime createdAt
) {
    public static RecordResponse from(TravelRecord record) {
        return new RecordResponse(
                record.getId(),
                record.getTitle(),
                record.getContent(),
                record.getImageUrl(),
                record.getVisitedAt(),
                record.getPlaceName(),
                record.getCreatedAt()
        );
    }
}

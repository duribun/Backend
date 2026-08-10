package duribun.be.domain.record.dto;

import duribun.be.domain.record.entity.TravelRecord;
import java.time.LocalDate;

public record RecordSummaryResponse(
        Long id,
        String title,
        String imageUrl,
        LocalDate visitedAt,
        String placeName
) {
    public static RecordSummaryResponse from(TravelRecord record) {
        return new RecordSummaryResponse(
                record.getId(),
                record.getTitle(),
                record.getImageUrl(),
                record.getVisitedAt(),
                record.getPlaceName()
        );
    }
}

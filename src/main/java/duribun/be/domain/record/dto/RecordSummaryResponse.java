package duribun.be.domain.record.dto;

import duribun.be.domain.record.entity.TravelRecord;

import java.time.LocalDate;

public record RecordSummaryResponse(
        Long id,
        String title,
        String thumbnailUrl,
        LocalDate visitedAt,
        String placeName,
        boolean favorite
) {
    public static RecordSummaryResponse of(TravelRecord record, String thumbnailUrl) {
        return new RecordSummaryResponse(
                record.getId(),
                record.getTitle(),
                thumbnailUrl,
                record.getVisitedAt(),
                record.getPlaceName(),
                record.isFavorite()
        );
    }
}

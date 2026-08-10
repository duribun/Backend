package duribun.be.domain.record.dto;

import java.time.LocalDate;

public record UpdateRecordRequest(
        String title,
        String content,
        String imageUrl,
        LocalDate visitedAt,
        String placeName
) {
}

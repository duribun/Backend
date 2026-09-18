package duribun.be.domain.location.dto;

import java.time.LocalDateTime;

public record VisitRecordResponse(
        Long regionId,
        String regionName,
        LocalDateTime visitedAt
) {
}

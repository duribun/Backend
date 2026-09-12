package duribun.be.domain.location.dto;

import java.util.List;

public record VerifyLocationResponse(
        boolean verified,
        boolean isFirstVisit,
        Long regionId,
        String regionName,
        double distanceMeters,
        List<NewlyAcquiredMascotResponse> newlyAcquiredMascots
) {
}

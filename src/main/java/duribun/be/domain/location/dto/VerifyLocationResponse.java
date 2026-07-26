package duribun.be.domain.location.dto;

public record VerifyLocationResponse(
        boolean verified,
        boolean isFirstVisit,
        Long regionId,
        String regionName,
        double distanceMeters
) {
}

package duribun.be.domain.location.dto;

import duribun.be.domain.location.entity.Region;

public record RegionResponse(
        Long id,
        String name,
        Double latitude,
        Double longitude,
        Integer verificationRadiusMeters
) {

    public static RegionResponse from(Region region) {
        return new RegionResponse(
                region.getId(),
                region.getName(),
                region.getLatitude(),
                region.getLongitude(),
                region.getVerificationRadiusMeters()
        );
    }
}

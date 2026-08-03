package duribun.be.domain.map.dto;

import duribun.be.domain.map.entity.Attraction;

public record AttractionSummaryResponse(
        String contentId,
        String title,
        Double latitude,
        Double longitude,
        String imageUrl
) {

    public static AttractionSummaryResponse from(Attraction attraction) {
        return new AttractionSummaryResponse(
                attraction.getContentId(),
                attraction.getTitle(),
                attraction.getLatitude(),
                attraction.getLongitude(),
                attraction.getImageUrl()
        );
    }
}

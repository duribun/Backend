package duribun.be.domain.map.dto;

import duribun.be.domain.map.entity.Attraction;
import java.util.List;

public record AttractionDetailResponse(
        String contentId,
        String title,
        String address,
        String description,
        List<String> images
) {

    public static AttractionDetailResponse of(Attraction attraction, List<String> images) {
        return new AttractionDetailResponse(
                attraction.getContentId(),
                attraction.getTitle(),
                attraction.getAddress(),
                attraction.getDescription(),
                images
        );
    }
}

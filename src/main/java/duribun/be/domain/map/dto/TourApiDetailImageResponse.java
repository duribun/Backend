package duribun.be.domain.map.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TourApiDetailImageResponse(
        @JsonProperty("originimgurl") String imageUrl
) {
}

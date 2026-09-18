package duribun.be.domain.map.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TourApiDetailCommonResponse(
        @JsonProperty("contentid") String contentId,
        @JsonProperty("contenttypeid") String contentTypeId,
        @JsonProperty("title") String title,
        @JsonProperty("addr1") String address,
        @JsonProperty("mapx") String longitude,
        @JsonProperty("mapy") String latitude,
        @JsonProperty("overview") String description,
        @JsonProperty("firstimage") String imageUrl
) {
}

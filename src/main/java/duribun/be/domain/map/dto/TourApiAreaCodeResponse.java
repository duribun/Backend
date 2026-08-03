package duribun.be.domain.map.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TourApiAreaCodeResponse(
        @JsonProperty("code") String code,
        @JsonProperty("name") String name
) {
}

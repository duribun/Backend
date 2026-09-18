package duribun.be.domain.location.dto;

import jakarta.validation.constraints.NotNull;

public record VerifyLocationRequest(
        @NotNull Long regionId,
        @NotNull Double latitude,
        @NotNull Double longitude
) {
}

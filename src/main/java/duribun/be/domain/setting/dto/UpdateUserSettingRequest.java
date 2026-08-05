package duribun.be.domain.setting.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateUserSettingRequest(
        @NotNull Boolean notificationEnabled
) {
}

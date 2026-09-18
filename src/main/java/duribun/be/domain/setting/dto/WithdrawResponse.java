package duribun.be.domain.setting.dto;

import duribun.be.domain.user.entity.UserStatus;

public record WithdrawResponse(
        String status
) {
    public static WithdrawResponse from(UserStatus status) {
        return new WithdrawResponse(status.name());
    }
}

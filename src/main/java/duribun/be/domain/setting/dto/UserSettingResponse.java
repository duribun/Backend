package duribun.be.domain.setting.dto;

import duribun.be.domain.setting.entity.UserSetting;

public record UserSettingResponse(
        Boolean notificationEnabled
) {
    public static UserSettingResponse from(UserSetting userSetting) {
        return new UserSettingResponse(userSetting.getNotificationEnabled());
    }
}

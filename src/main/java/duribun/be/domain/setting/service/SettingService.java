package duribun.be.domain.setting.service;

import duribun.be.domain.auth.service.AuthService;
import duribun.be.domain.setting.dto.ProfileUpdateRequest;
import duribun.be.domain.setting.dto.ProfileUpdateResponse;
import duribun.be.domain.setting.dto.UserSettingResponse;
import duribun.be.domain.setting.dto.WithdrawResponse;
import duribun.be.domain.setting.entity.UserSetting;
import duribun.be.domain.setting.repository.UserSettingRepository;
import duribun.be.domain.user.entity.User;
import duribun.be.domain.user.entity.UserStatus;
import duribun.be.domain.user.service.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SettingService {

    private final UserSettingRepository userSettingRepository;
    private final UserService userService;
    private final AuthService authService;

    public SettingService(UserSettingRepository userSettingRepository,
                           UserService userService,
                           AuthService authService) {
        this.userSettingRepository = userSettingRepository;
        this.userService = userService;
        this.authService = authService;
    }

    public UserSettingResponse getMySetting(Long userId) {
        return UserSettingResponse.from(findOrCreateSetting(userId));
    }

    public UserSettingResponse updateMySetting(Long userId, boolean notificationEnabled) {
        UserSetting setting = findOrCreateSetting(userId);
        setting.updateNotificationEnabled(notificationEnabled);
        return UserSettingResponse.from(setting);
    }

    public WithdrawResponse withdraw(Long userId) {
        userService.withdraw(userId);
        authService.revokeAllTokens(userId);
        return WithdrawResponse.from(UserStatus.WITHDRAWN);
    }

    public boolean checkNicknameAvailability(Long userId, String nickname) {
        return userService.isNicknameAvailable(userId, nickname);
    }

    public ProfileUpdateResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userService.updateProfile(userId, request.nickname(), request.birthDate(), request.gender());
        return ProfileUpdateResponse.from(user);
    }

    private UserSetting findOrCreateSetting(Long userId) {
        return userSettingRepository.findByUserId(userId)
                .orElseGet(() -> createSetting(userId));
    }

    private UserSetting createSetting(Long userId) {
        try {
            return userSettingRepository.save(UserSetting.create(userId));
        } catch (DataIntegrityViolationException e) {
            // 동시에 같은 유저의 설정이 먼저 생성된 경우: 실제 설정을 재조회한다
            return userSettingRepository.findByUserId(userId).orElseThrow(() -> e);
        }
    }
}

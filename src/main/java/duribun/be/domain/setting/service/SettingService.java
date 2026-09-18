package duribun.be.domain.setting.service;

import duribun.be.domain.auth.service.AuthService;
import duribun.be.domain.setting.dto.UserSettingResponse;
import duribun.be.domain.setting.dto.WithdrawResponse;
import duribun.be.domain.setting.entity.UserSetting;
import duribun.be.domain.setting.repository.UserSettingRepository;
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
    private final AccountResetService accountResetService;

    public SettingService(UserSettingRepository userSettingRepository,
                           UserService userService,
                           AuthService authService,
                           AccountResetService accountResetService) {
        this.userSettingRepository = userSettingRepository;
        this.userService = userService;
        this.authService = authService;
        this.accountResetService = accountResetService;
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
        // 이 클래스가 클래스 레벨 @Transactional이라, 아래 두 호출과 위 withdraw()가 하나의 트랜잭션으로
        // 묶여서 일부만 처리되고 실패하는 상황(예: 데이터는 지워졌는데 탈퇴 상태는 롤백)이 생기지 않는다.
        accountResetService.resetUserData(userId);
        authService.revokeAllTokens(userId);
        return WithdrawResponse.from(UserStatus.WITHDRAWN);
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

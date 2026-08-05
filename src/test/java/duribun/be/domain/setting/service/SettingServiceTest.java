package duribun.be.domain.setting.service;

import duribun.be.domain.auth.service.AuthService;
import duribun.be.domain.setting.dto.UserSettingResponse;
import duribun.be.domain.setting.entity.UserSetting;
import duribun.be.domain.setting.repository.UserSettingRepository;
import duribun.be.domain.setting.dto.WithdrawResponse;
import duribun.be.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettingServiceTest {

    @Mock
    private UserSettingRepository userSettingRepository;
    @Mock
    private UserService userService;
    @Mock
    private AuthService authService;

    private SettingService settingService;

    @BeforeEach
    void setUp() {
        settingService = new SettingService(userSettingRepository, userService, authService);
    }

    @Test
    void getMySetting_설정이_없으면_기본값으로_생성해서_저장한다() {
        when(userSettingRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userSettingRepository.save(any(UserSetting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserSettingResponse response = settingService.getMySetting(1L);

        assertThat(response.notificationEnabled()).isTrue();
        verify(userSettingRepository).save(any(UserSetting.class));
    }

    @Test
    void getMySetting_설정이_있으면_기존_값을_반환한다() {
        UserSetting setting = UserSetting.create(1L);
        setting.updateNotificationEnabled(false);
        when(userSettingRepository.findByUserId(1L)).thenReturn(Optional.of(setting));

        UserSettingResponse response = settingService.getMySetting(1L);

        assertThat(response.notificationEnabled()).isFalse();
        verify(userSettingRepository, never()).save(any(UserSetting.class));
    }

    @Test
    void getMySetting_동시에_같은_유저의_설정이_먼저_생성되면_재조회해_반환한다() {
        UserSetting racedSetting = UserSetting.create(1L);
        when(userSettingRepository.findByUserId(1L))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(racedSetting));
        when(userSettingRepository.save(any(UserSetting.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate user_id"));

        UserSettingResponse response = settingService.getMySetting(1L);

        assertThat(response.notificationEnabled()).isTrue();
    }

    @Test
    void updateMySetting_알림설정을_변경한다() {
        UserSetting setting = UserSetting.create(1L);
        when(userSettingRepository.findByUserId(1L)).thenReturn(Optional.of(setting));

        UserSettingResponse response = settingService.updateMySetting(1L, false);

        assertThat(response.notificationEnabled()).isFalse();
        assertThat(setting.getNotificationEnabled()).isFalse();
    }

    @Test
    void withdraw_UserService와_AuthService를_순서대로_호출한다() {
        WithdrawResponse response = settingService.withdraw(1L);

        InOrder inOrder = inOrder(userService, authService);
        inOrder.verify(userService).withdraw(1L);
        inOrder.verify(authService).revokeAllTokens(1L);
        assertThat(response.status()).isEqualTo("WITHDRAWN");
    }
}

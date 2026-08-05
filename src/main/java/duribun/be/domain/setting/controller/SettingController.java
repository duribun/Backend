package duribun.be.domain.setting.controller;

import duribun.be.domain.setting.dto.UpdateUserSettingRequest;
import duribun.be.domain.setting.dto.UserSettingResponse;
import duribun.be.domain.setting.dto.WithdrawResponse;
import duribun.be.domain.setting.service.SettingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
public class SettingController {

    private final SettingService settingService;

    public SettingController(SettingService settingService) {
        this.settingService = settingService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserSettingResponse> getMySetting(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(settingService.getMySetting(userId));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserSettingResponse> updateMySetting(@AuthenticationPrincipal Long userId,
                                                                 @Valid @RequestBody UpdateUserSettingRequest request) {
        return ResponseEntity.ok(settingService.updateMySetting(userId, request.notificationEnabled()));
    }

    @DeleteMapping("/me/withdraw")
    public ResponseEntity<WithdrawResponse> withdraw(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(settingService.withdraw(userId));
    }
}

package duribun.be.domain.setting.controller;

import duribun.be.domain.setting.dto.NicknameCheckResponse;
import duribun.be.domain.setting.dto.ProfileUpdateRequest;
import duribun.be.domain.setting.dto.ProfileUpdateResponse;
import duribun.be.domain.setting.service.SettingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final SettingService settingService;

    public UserProfileController(SettingService settingService) {
        this.settingService = settingService;
    }

    @GetMapping("/nickname-check")
    public ResponseEntity<NicknameCheckResponse> checkNickname(@AuthenticationPrincipal Long userId,
                                                                @RequestParam String nickname) {
        return ResponseEntity.ok(NicknameCheckResponse.of(settingService.checkNicknameAvailability(userId, nickname)));
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<ProfileUpdateResponse> updateProfile(@AuthenticationPrincipal Long userId,
                                                                @Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(settingService.updateProfile(userId, request));
    }
}

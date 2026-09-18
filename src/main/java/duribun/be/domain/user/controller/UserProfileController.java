package duribun.be.domain.user.controller;

import duribun.be.domain.user.dto.NicknameCheckResponse;
import duribun.be.domain.user.dto.ProfileUpdateRequest;
import duribun.be.domain.user.dto.ProfileUpdateResponse;
import duribun.be.domain.user.dto.UserProfileResponse;
import duribun.be.domain.user.entity.User;
import duribun.be.domain.user.service.UserService;
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

    private final UserService userService;

    public UserProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/nickname-check")
    public ResponseEntity<NicknameCheckResponse> checkNickname(@AuthenticationPrincipal Long userId,
                                                                @RequestParam String nickname) {
        return ResponseEntity.ok(NicknameCheckResponse.of(userService.isNicknameAvailable(userId, nickname)));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(@AuthenticationPrincipal Long userId) {
        User user = userService.getMyProfile(userId);
        return ResponseEntity.ok(UserProfileResponse.from(user));
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<ProfileUpdateResponse> updateProfile(@AuthenticationPrincipal Long userId,
                                                                @Valid @RequestBody ProfileUpdateRequest request) {
        User user = userService.updateProfile(userId, request.nickname(), request.birthDate(), request.gender());
        return ResponseEntity.ok(ProfileUpdateResponse.from(user));
    }
}

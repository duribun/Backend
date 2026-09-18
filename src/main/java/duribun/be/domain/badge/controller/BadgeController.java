package duribun.be.domain.badge.controller;

import duribun.be.domain.badge.dto.BadgeResponse;
import duribun.be.domain.badge.dto.MyBadgeResponse;
import duribun.be.domain.badge.service.BadgeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/badges")
public class BadgeController {

    private final BadgeService badgeService;

    public BadgeController(BadgeService badgeService) {
        this.badgeService = badgeService;
    }

    @GetMapping
    public ResponseEntity<List<BadgeResponse>> getBadges(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(badgeService.getAllBadges(userId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<MyBadgeResponse>> getMyBadges(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(badgeService.getMyBadges(userId));
    }
}

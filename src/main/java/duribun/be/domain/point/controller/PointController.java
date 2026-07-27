package duribun.be.domain.point.controller;

import duribun.be.domain.point.dto.PointBalanceResponse;
import duribun.be.domain.point.dto.PointHistoryResponse;
import duribun.be.domain.point.service.PointServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/points")
public class PointController {

    private final PointServiceImpl pointService;

    public PointController(PointServiceImpl pointService) {
        this.pointService = pointService;
    }

    @GetMapping("/me")
    public ResponseEntity<PointBalanceResponse> getMyBalance(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(PointBalanceResponse.from(pointService.getBalance(userId)));
    }

    @GetMapping("/history")
    public ResponseEntity<Page<PointHistoryResponse>> getMyHistory(
            @AuthenticationPrincipal Long userId,
            @PageableDefault(size = 20, sort = {"createdAt", "id"}, direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(pointService.getHistory(userId, pageable));
    }
}

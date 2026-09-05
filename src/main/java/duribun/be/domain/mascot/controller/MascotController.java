package duribun.be.domain.mascot.controller;

import duribun.be.domain.mascot.dto.MascotResponse;
import duribun.be.domain.mascot.dto.MyMascotResponse;
import duribun.be.domain.mascot.service.MascotService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mascots")
public class MascotController {

    private final MascotService mascotService;

    public MascotController(MascotService mascotService) {
        this.mascotService = mascotService;
    }

    @GetMapping
    public ResponseEntity<List<MascotResponse>> getMascots(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(mascotService.getAllMascots(userId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<MyMascotResponse>> getMyMascots(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(mascotService.getMyMascots(userId));
    }
}

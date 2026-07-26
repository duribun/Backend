package duribun.be.domain.location.controller;

import duribun.be.domain.location.dto.RegionResponse;
import duribun.be.domain.location.dto.VerifyLocationRequest;
import duribun.be.domain.location.dto.VerifyLocationResponse;
import duribun.be.domain.location.dto.VisitRecordResponse;
import duribun.be.domain.location.service.LocationVerificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationVerificationService locationVerificationService;

    public LocationController(LocationVerificationService locationVerificationService) {
        this.locationVerificationService = locationVerificationService;
    }

    @GetMapping("/regions")
    public ResponseEntity<List<RegionResponse>> getRegions() {
        return ResponseEntity.ok(locationVerificationService.getAllRegions());
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifyLocationResponse> verify(@AuthenticationPrincipal Long userId,
                                                          @Valid @RequestBody VerifyLocationRequest request) {
        return ResponseEntity.ok(locationVerificationService.verify(userId, request));
    }

    @GetMapping("/visits")
    public ResponseEntity<List<VisitRecordResponse>> getVisits(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(locationVerificationService.getVisits(userId));
    }
}

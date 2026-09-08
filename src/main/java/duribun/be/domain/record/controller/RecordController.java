package duribun.be.domain.record.controller;

import duribun.be.domain.record.dto.CreateRecordRequest;
import duribun.be.domain.record.dto.PresignedImageUploadResponse;
import duribun.be.domain.record.dto.RecordResponse;
import duribun.be.domain.record.dto.RecordSummaryResponse;
import duribun.be.domain.record.dto.UpdateRecordRequest;
import duribun.be.domain.record.service.RecordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/records")
public class RecordController {

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @PostMapping("/images/presigned-url")
    public ResponseEntity<PresignedImageUploadResponse> issuePresignedUrl(@AuthenticationPrincipal Long userId,
                                                                           @RequestParam String extension) {
        return ResponseEntity.ok(recordService.issuePresignedUrl(userId, extension));
    }

    @PostMapping
    public ResponseEntity<RecordResponse> create(@AuthenticationPrincipal Long userId,
                                                  @RequestBody @Valid CreateRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recordService.createRecord(userId, request));
    }

    @GetMapping("/me")
    public ResponseEntity<List<RecordSummaryResponse>> getMyRecords(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(recordService.getMyRecords(userId, year, month));
    }

    @GetMapping("/{recordId}")
    public ResponseEntity<RecordResponse> getRecord(@AuthenticationPrincipal Long userId,
                                                     @PathVariable Long recordId) {
        return ResponseEntity.ok(recordService.getRecord(userId, recordId));
    }

    @PatchMapping("/{recordId}")
    public ResponseEntity<RecordResponse> update(@AuthenticationPrincipal Long userId,
                                                  @PathVariable Long recordId,
                                                  @RequestBody @Valid UpdateRecordRequest request) {
        return ResponseEntity.ok(recordService.updateRecord(userId, recordId, request));
    }

    @PatchMapping("/{recordId}/favorite")
    public ResponseEntity<RecordResponse> toggleFavorite(@AuthenticationPrincipal Long userId,
                                                          @PathVariable Long recordId) {
        return ResponseEntity.ok(recordService.toggleFavorite(userId, recordId));
    }

    @DeleteMapping("/{recordId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Long userId,
                                        @PathVariable Long recordId) {
        recordService.deleteRecord(userId, recordId);
        return ResponseEntity.noContent().build();
    }
}

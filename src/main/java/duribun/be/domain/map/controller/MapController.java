package duribun.be.domain.map.controller;

import duribun.be.domain.map.dto.AttractionDetailResponse;
import duribun.be.domain.map.dto.AttractionSummaryResponse;
import duribun.be.domain.map.service.MapService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/map")
public class MapController {

    private final MapService mapService;

    public MapController(MapService mapService) {
        this.mapService = mapService;
    }

    @GetMapping("/regions/{regionId}/attractions")
    public ResponseEntity<List<AttractionSummaryResponse>> getRegionAttractions(@PathVariable Long regionId) {
        return ResponseEntity.ok(mapService.getAttractionsByRegion(regionId));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<AttractionSummaryResponse>> getNearbyAttractions(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(required = false) Integer radiusMeters) {
        return ResponseEntity.ok(mapService.getNearbyAttractions(latitude, longitude, radiusMeters));
    }

    @GetMapping("/search")
    public ResponseEntity<List<AttractionSummaryResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(mapService.searchAttractions(keyword));
    }

    @GetMapping("/attractions/{contentId}")
    public ResponseEntity<AttractionDetailResponse> getAttractionDetail(@PathVariable String contentId) {
        return ResponseEntity.ok(mapService.getAttractionDetail(contentId));
    }
}

package duribun.be.domain.place.controller;

import duribun.be.domain.place.dto.PlaceSearchResponse;
import duribun.be.domain.place.service.PlaceSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/places")
public class PlaceController {

    private final PlaceSearchService placeSearchService;

    public PlaceController(PlaceSearchService placeSearchService) {
        this.placeSearchService = placeSearchService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<PlaceSearchResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(placeSearchService.search(keyword));
    }
}

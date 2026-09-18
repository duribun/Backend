package duribun.be.domain.map.service;

import duribun.be.domain.location.repository.RegionRepository;
import duribun.be.domain.map.client.TourApiClient;
import duribun.be.domain.map.dto.AttractionDetailResponse;
import duribun.be.domain.map.dto.AttractionSummaryResponse;
import duribun.be.domain.map.dto.TourApiDetailCommonResponse;
import duribun.be.domain.map.dto.TourApiDetailImageResponse;
import duribun.be.domain.map.dto.TourApiItemResponse;
import duribun.be.domain.map.entity.Attraction;
import duribun.be.domain.map.entity.TourApiRegionMapping;
import duribun.be.domain.map.repository.AttractionRepository;
import duribun.be.domain.map.repository.TourApiRegionMappingRepository;
import duribun.be.global.exception.AttractionNotFoundException;
import duribun.be.global.exception.RegionMappingNotFoundException;
import duribun.be.global.exception.RegionNotFoundException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MapService {

    private static final Logger log = LoggerFactory.getLogger(MapService.class);

    private static final String TOURIST_SPOT_CONTENT_TYPE_ID = "12";
    private static final int DEFAULT_RADIUS_METERS = 5000;
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    private final TourApiClient tourApiClient;
    private final AttractionRepository attractionRepository;
    private final RegionRepository regionRepository;
    private final TourApiRegionMappingRepository tourApiRegionMappingRepository;

    public MapService(TourApiClient tourApiClient, AttractionRepository attractionRepository,
                       RegionRepository regionRepository,
                       TourApiRegionMappingRepository tourApiRegionMappingRepository) {
        this.tourApiClient = tourApiClient;
        this.attractionRepository = attractionRepository;
        this.regionRepository = regionRepository;
        this.tourApiRegionMappingRepository = tourApiRegionMappingRepository;
    }

    public List<AttractionSummaryResponse> getAttractionsByRegion(Long regionId) {
        regionRepository.findById(regionId)
                .orElseThrow(() -> new RegionNotFoundException("존재하지 않는 지역입니다"));

        List<Attraction> cached = attractionRepository.findByRegionId(regionId);
        if (!cached.isEmpty() && isFresh(cached)) {
            return toSummaryList(cached);
        }

        try {
            TourApiRegionMapping mapping = tourApiRegionMappingRepository.findByRegionId(regionId)
                    .orElseThrow(() -> new RegionMappingNotFoundException(
                            "TourAPI 지역코드 매핑이 없습니다: regionId=" + regionId));
            List<TourApiItemResponse> items = tourApiClient.areaBasedList(
                    mapping.getTourApiAreaCode(), mapping.getTourApiSigunguCode(), TOURIST_SPOT_CONTENT_TYPE_ID);
            List<Attraction> upserted = items.stream().map(item -> upsert(item, regionId)).toList();
            return toSummaryList(upserted);
        } catch (RegionMappingNotFoundException e) {
            log.warn(e.getMessage());
            return List.of();
        }
    }

    public List<AttractionSummaryResponse> getNearbyAttractions(double latitude, double longitude,
                                                                  Integer radiusMeters) {
        int radius = radiusMeters != null ? radiusMeters : DEFAULT_RADIUS_METERS;
        List<TourApiItemResponse> items = tourApiClient.locationBasedList(latitude, longitude, radius,
                TOURIST_SPOT_CONTENT_TYPE_ID);
        List<Attraction> upserted = items.stream().map(item -> upsert(item, null)).toList();
        return toSummaryList(upserted);
    }

    public List<AttractionSummaryResponse> searchAttractions(String keyword) {
        List<TourApiItemResponse> items = tourApiClient.searchKeyword(keyword, TOURIST_SPOT_CONTENT_TYPE_ID);
        List<Attraction> upserted = items.stream().map(item -> upsert(item, null)).toList();
        return toSummaryList(upserted);
    }

    public AttractionDetailResponse getAttractionDetail(String contentId) {
        LocalDateTime threshold = LocalDateTime.now().minus(CACHE_TTL);
        Optional<Attraction> cached = attractionRepository.findByContentId(contentId);

        if (cached.isPresent() && !cached.get().isStale(threshold)) {
            return AttractionDetailResponse.of(cached.get(), fetchImages(contentId));
        }

        TourApiDetailCommonResponse common = tourApiClient.detailCommon(contentId)
                .orElseThrow(() -> new AttractionNotFoundException("존재하지 않는 관광지입니다: contentId=" + contentId));
        List<String> images = fetchImages(contentId);
        String imageUrl = images.isEmpty() ? null : images.get(0);
        Double latitude = parseDouble(common.latitude());
        Double longitude = parseDouble(common.longitude());

        Attraction attraction = cached.orElseGet(() -> Attraction.create(contentId, null, common.contentTypeId(),
                common.title(), common.address(), latitude, longitude, common.description(), imageUrl));
        cached.ifPresent(existing -> existing.updateFromTourApi(common.title(), common.address(), latitude,
                longitude, common.description(), imageUrl));
        attractionRepository.save(attraction);

        return AttractionDetailResponse.of(attraction, images);
    }

    private List<String> fetchImages(String contentId) {
        return tourApiClient.detailImage(contentId).stream()
                .map(TourApiDetailImageResponse::imageUrl)
                .toList();
    }

    private Attraction upsert(TourApiItemResponse item, Long regionId) {
        Double latitude = parseDouble(item.latitude());
        Double longitude = parseDouble(item.longitude());
        Attraction attraction = attractionRepository.findByContentId(item.contentId())
                .map(existing -> {
                    existing.updateFromTourApi(item.title(), item.address(), latitude, longitude,
                            existing.getDescription(), item.imageUrl());
                    if (regionId != null) {
                        existing.assignRegion(regionId);
                    }
                    return existing;
                })
                .orElseGet(() -> Attraction.create(item.contentId(), regionId, item.contentTypeId(), item.title(),
                        item.address(), latitude, longitude, null, item.imageUrl()));
        return attractionRepository.save(attraction);
    }

    private boolean isFresh(List<Attraction> attractions) {
        LocalDateTime threshold = LocalDateTime.now().minus(CACHE_TTL);
        return attractions.stream().noneMatch(attraction -> attraction.isStale(threshold));
    }

    private List<AttractionSummaryResponse> toSummaryList(List<Attraction> attractions) {
        return attractions.stream().map(AttractionSummaryResponse::from).toList();
    }

    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Double.parseDouble(value);
    }
}

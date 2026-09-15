package duribun.be.domain.place.service;

import duribun.be.domain.map.client.TourApiClient;
import duribun.be.domain.map.dto.TourApiItemResponse;
import duribun.be.domain.place.dto.PlaceSearchResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 기록 작성 화면의 "방문 장소" 검색 전용 서비스.
 * map 도메인의 TourApiClient(searchKeyword)를 그대로 재사용하되, MapService.searchAttractions()와는
 * 다르게 동작한다:
 * - contentTypeId를 관광지(12)로 제한하지 않는다 - 음식점/숙박/문화시설 등도 "방문 장소"가 될 수 있다.
 * - Attraction 테이블에 캐싱하지 않는다 - 단순 프록시라 검색 결과를 영구 저장할 이유가 없고,
 *   전체 콘텐츠 타입을 다 캐싱하면 map 탭의 관광지 캐시(24시간 TTL) 의미가 옅어진다.
 */
@Service
public class PlaceSearchService {

    private static final int MIN_KEYWORD_LENGTH = 2;

    private final TourApiClient tourApiClient;

    public PlaceSearchService(TourApiClient tourApiClient) {
        this.tourApiClient = tourApiClient;
    }

    public List<PlaceSearchResponse> search(String keyword) {
        if (keyword == null || keyword.trim().length() < MIN_KEYWORD_LENGTH) {
            return List.of();
        }
        List<TourApiItemResponse> items = tourApiClient.searchKeyword(keyword.trim(), null);
        return items.stream().map(PlaceSearchResponse::from).toList();
    }
}

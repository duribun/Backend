package duribun.be.domain.map.service;

import duribun.be.domain.location.entity.Region;
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
import duribun.be.global.exception.RegionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MapServiceTest {

    @Mock
    private TourApiClient tourApiClient;
    @Mock
    private AttractionRepository attractionRepository;
    @Mock
    private RegionRepository regionRepository;
    @Mock
    private TourApiRegionMappingRepository tourApiRegionMappingRepository;

    private MapService mapService;

    @BeforeEach
    void setUp() {
        mapService = new MapService(tourApiClient, attractionRepository, regionRepository,
                tourApiRegionMappingRepository);
    }

    private Attraction attraction(String contentId, Long regionId, LocalDateTime syncedAt) throws Exception {
        Attraction attraction = Attraction.create(contentId, regionId, "12", "제목", "주소", 1.0, 1.0, null, null);
        setField(attraction, "syncedAt", syncedAt);
        return attraction;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void getAttractionsByRegion_존재하지_않는_regionId면_RegionNotFoundException을_던진다() {
        when(regionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mapService.getAttractionsByRegion(1L))
                .isInstanceOf(RegionNotFoundException.class);
    }

    @Test
    void getAttractionsByRegion_캐시가_모두_fresh하면_TourAPI를_호출하지_않는다() throws Exception {
        when(regionRepository.findById(1L)).thenReturn(Optional.of(mockRegion()));
        Attraction fresh = attraction("1", 1L, LocalDateTime.now().minusHours(1));
        when(attractionRepository.findByRegionId(1L)).thenReturn(List.of(fresh));

        List<AttractionSummaryResponse> result = mapService.getAttractionsByRegion(1L);

        assertThat(result).hasSize(1);
        verify(tourApiClient, never()).areaBasedList(anyString(), any(), anyString());
    }

    @Test
    void getAttractionsByRegion_캐시가_stale하면_TourAPI를_호출해_갱신한다() throws Exception {
        when(regionRepository.findById(1L)).thenReturn(Optional.of(mockRegion()));
        Attraction stale = attraction("1", 1L, LocalDateTime.now().minusHours(25));
        when(attractionRepository.findByRegionId(1L)).thenReturn(List.of(stale));
        when(tourApiRegionMappingRepository.findByRegionId(1L))
                .thenReturn(Optional.of(TourApiRegionMapping.create(1L, "32", "1")));
        when(tourApiClient.areaBasedList("32", "1", "12")).thenReturn(List.of(
                new TourApiItemResponse("1", "12", "경포대", "강원 강릉시", "128.90", "37.79", "https://a")));
        when(attractionRepository.findByContentId("1")).thenReturn(Optional.of(stale));
        when(attractionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<AttractionSummaryResponse> result = mapService.getAttractionsByRegion(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("경포대");
        verify(tourApiClient).areaBasedList("32", "1", "12");
    }

    @Test
    void getAttractionsByRegion_지역코드_매핑이_없으면_빈_리스트를_반환하고_예외를_전파하지_않는다() {
        when(regionRepository.findById(1L)).thenReturn(Optional.of(mockRegion()));
        when(attractionRepository.findByRegionId(1L)).thenReturn(List.of());
        when(tourApiRegionMappingRepository.findByRegionId(1L)).thenReturn(Optional.empty());

        List<AttractionSummaryResponse> result = mapService.getAttractionsByRegion(1L);

        assertThat(result).isEmpty();
        verify(tourApiClient, never()).areaBasedList(anyString(), any(), anyString());
    }

    @Test
    void getNearbyAttractions_radius가_없으면_기본값_5000으로_호출한다() {
        when(tourApiClient.locationBasedList(37.79, 128.9, 5000, "12")).thenReturn(List.of());

        mapService.getNearbyAttractions(37.79, 128.9, null);

        verify(tourApiClient).locationBasedList(37.79, 128.9, 5000, "12");
    }

    @Test
    void getNearbyAttractions_radius가_있으면_그대로_전달한다() {
        when(tourApiClient.locationBasedList(37.79, 128.9, 1000, "12")).thenReturn(List.of());

        mapService.getNearbyAttractions(37.79, 128.9, 1000);

        verify(tourApiClient).locationBasedList(37.79, 128.9, 1000, "12");
    }

    @Test
    void searchAttractions_TourAPI_결과를_요약_응답으로_변환한다() {
        when(tourApiClient.searchKeyword("경포대", "12")).thenReturn(List.of(
                new TourApiItemResponse("1", "12", "경포대", "강원 강릉시", "128.90", "37.79", "https://a")));
        when(attractionRepository.findByContentId("1")).thenReturn(Optional.empty());
        when(attractionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<AttractionSummaryResponse> result = mapService.searchAttractions("경포대");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).contentId()).isEqualTo("1");
    }

    @Test
    void getAttractionDetail_캐시가_fresh해도_이미지는_항상_라이브로_조회한다() throws Exception {
        Attraction fresh = attraction("1", 1L, LocalDateTime.now().minusHours(1));
        when(attractionRepository.findByContentId("1")).thenReturn(Optional.of(fresh));
        when(tourApiClient.detailImage("1")).thenReturn(List.of(new TourApiDetailImageResponse("https://img")));

        AttractionDetailResponse result = mapService.getAttractionDetail("1");

        assertThat(result.images()).containsExactly("https://img");
        verify(tourApiClient, never()).detailCommon(anyString());
    }

    @Test
    void getAttractionDetail_캐시가_stale하면_상세정보를_재조회해서_갱신한다() throws Exception {
        Attraction stale = attraction("1", 1L, LocalDateTime.now().minusHours(25));
        when(attractionRepository.findByContentId("1")).thenReturn(Optional.of(stale));
        when(tourApiClient.detailCommon("1")).thenReturn(Optional.of(
                new TourApiDetailCommonResponse("1", "12", "새제목", "새주소", "128.90", "37.79", "새설명", "https://new")));
        when(tourApiClient.detailImage("1")).thenReturn(List.of(new TourApiDetailImageResponse("https://new")));
        when(attractionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AttractionDetailResponse result = mapService.getAttractionDetail("1");

        assertThat(result.title()).isEqualTo("새제목");
        assertThat(result.description()).isEqualTo("새설명");
        verify(attractionRepository).save(any());
    }

    @Test
    void getAttractionDetail_캐시에_없고_TourAPI에도_없으면_AttractionNotFoundException을_던진다() {
        when(attractionRepository.findByContentId("no-such-id")).thenReturn(Optional.empty());
        when(tourApiClient.detailCommon("no-such-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mapService.getAttractionDetail("no-such-id"))
                .isInstanceOf(AttractionNotFoundException.class);
    }

    private Region mockRegion() {
        return Region.create("51150", "강릉시", 37.7519, 128.8761, 1000);
    }
}

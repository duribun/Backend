package duribun.be.domain.place.service;

import duribun.be.domain.map.client.TourApiClient;
import duribun.be.domain.map.dto.TourApiItemResponse;
import duribun.be.domain.place.dto.PlaceSearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceSearchServiceTest {

    @Mock
    private TourApiClient tourApiClient;

    private PlaceSearchService placeSearchService;

    @BeforeEach
    void setUp() {
        placeSearchService = new PlaceSearchService(tourApiClient);
    }

    @Test
    void 검색어가_2자_이상이면_TourAPI를_호출해_결과를_반환한다() {
        when(tourApiClient.searchKeyword("경복궁", null)).thenReturn(List.of(
                new TourApiItemResponse("1", "12", "경복궁", "서울 종로구 사직로 161", "126.977", "37.579", "https://a")));

        List<PlaceSearchResponse> results = placeSearchService.search("경복궁");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).placeName()).isEqualTo("경복궁");
        assertThat(results.get(0).address()).isEqualTo("서울 종로구 사직로 161");
        assertThat(results.get(0).latitude()).isEqualTo(37.579);
        assertThat(results.get(0).longitude()).isEqualTo(126.977);
    }

    @Test
    void 검색어_앞뒤_공백은_트림해서_호출한다() {
        when(tourApiClient.searchKeyword("경복궁", null)).thenReturn(List.of());

        placeSearchService.search("  경복궁  ");

        verify(tourApiClient).searchKeyword("경복궁", null);
    }

    @Test
    void 검색어가_1자_이하면_TourAPI를_호출하지_않고_빈_리스트를_반환한다() {
        List<PlaceSearchResponse> results = placeSearchService.search("가");

        assertThat(results).isEmpty();
        verify(tourApiClient, never()).searchKeyword(anyString(), any());
    }

    @Test
    void 검색어가_null이면_TourAPI를_호출하지_않고_빈_리스트를_반환한다() {
        List<PlaceSearchResponse> results = placeSearchService.search(null);

        assertThat(results).isEmpty();
        verify(tourApiClient, never()).searchKeyword(anyString(), any());
    }
}

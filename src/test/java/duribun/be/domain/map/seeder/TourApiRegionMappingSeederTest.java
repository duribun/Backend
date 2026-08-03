package duribun.be.domain.map.seeder;

import duribun.be.domain.location.entity.Region;
import duribun.be.domain.location.repository.RegionRepository;
import duribun.be.domain.map.client.TourApiClient;
import duribun.be.domain.map.dto.TourApiAreaCodeResponse;
import duribun.be.domain.map.entity.TourApiRegionMapping;
import duribun.be.domain.map.repository.TourApiRegionMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TourApiRegionMappingSeederTest {

    @Mock
    private TourApiClient tourApiClient;
    @Mock
    private RegionRepository regionRepository;
    @Mock
    private TourApiRegionMappingRepository tourApiRegionMappingRepository;

    private TourApiRegionMappingSeeder seeder;

    @BeforeEach
    void setUp() {
        seeder = new TourApiRegionMappingSeeder(tourApiClient, regionRepository, tourApiRegionMappingRepository);
    }

    private Region region(Long id, String name) throws Exception {
        Region region = Region.create("00000", name, 0.0, 0.0, 1000);
        var field = Region.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(region, id);
        return region;
    }

    @Test
    void 광역시_단위_이름은_시도목록에서_바로_매칭되어_시군구코드없이_저장된다() throws Exception {
        Region seoul = region(1L, "서울특별시");
        when(regionRepository.findAll()).thenReturn(List.of(seoul));
        when(tourApiRegionMappingRepository.findByRegionId(1L)).thenReturn(Optional.empty());
        when(tourApiClient.areaCode(null)).thenReturn(List.of(
                new TourApiAreaCodeResponse("1", "서울"),
                new TourApiAreaCodeResponse("32", "강원특별자치도")
        ));

        seeder.seed();

        ArgumentCaptor<TourApiRegionMapping> captor = ArgumentCaptor.forClass(TourApiRegionMapping.class);
        verify(tourApiRegionMappingRepository).save(captor.capture());
        assertThat(captor.getValue().getRegionId()).isEqualTo(1L);
        assertThat(captor.getValue().getTourApiAreaCode()).isEqualTo("1");
        assertThat(captor.getValue().getTourApiSigunguCode()).isNull();
        verify(tourApiClient, never()).areaCode(eq("1"));
        verify(tourApiClient, never()).areaCode(eq("32"));
    }

    @Test
    void 시군구_단위_이름은_각_시도의_시군구목록을_조회해_매칭되면_저장된다() throws Exception {
        Region gangneung = region(2L, "강릉시");
        when(regionRepository.findAll()).thenReturn(List.of(gangneung));
        when(tourApiRegionMappingRepository.findByRegionId(2L)).thenReturn(Optional.empty());
        when(tourApiClient.areaCode(null)).thenReturn(List.of(
                new TourApiAreaCodeResponse("1", "서울"),
                new TourApiAreaCodeResponse("32", "강원특별자치도")
        ));
        when(tourApiClient.areaCode("1")).thenReturn(List.of(new TourApiAreaCodeResponse("1", "종로구")));
        when(tourApiClient.areaCode("32")).thenReturn(List.of(new TourApiAreaCodeResponse("1", "강릉시")));

        seeder.seed();

        ArgumentCaptor<TourApiRegionMapping> captor = ArgumentCaptor.forClass(TourApiRegionMapping.class);
        verify(tourApiRegionMappingRepository).save(captor.capture());
        assertThat(captor.getValue().getRegionId()).isEqualTo(2L);
        assertThat(captor.getValue().getTourApiAreaCode()).isEqualTo("32");
        assertThat(captor.getValue().getTourApiSigunguCode()).isEqualTo("1");
    }

    @Test
    void 매칭에_실패하면_저장하지_않고_넘어간다() throws Exception {
        Region unknown = region(3L, "존재하지않는지역");
        when(regionRepository.findAll()).thenReturn(List.of(unknown));
        when(tourApiRegionMappingRepository.findByRegionId(3L)).thenReturn(Optional.empty());
        when(tourApiClient.areaCode(null)).thenReturn(List.of(new TourApiAreaCodeResponse("1", "서울")));
        when(tourApiClient.areaCode("1")).thenReturn(List.of(new TourApiAreaCodeResponse("1", "종로구")));

        seeder.seed();

        verify(tourApiRegionMappingRepository, never()).save(any());
    }

    @Test
    void 이미_매핑이_있는_Region은_건너뛴다() throws Exception {
        Region seoul = region(1L, "서울특별시");
        when(regionRepository.findAll()).thenReturn(List.of(seoul));
        when(tourApiRegionMappingRepository.findByRegionId(1L))
                .thenReturn(Optional.of(TourApiRegionMapping.create(1L, "1", null)));
        when(tourApiClient.areaCode(isNull())).thenReturn(List.of(new TourApiAreaCodeResponse("1", "서울")));

        seeder.seed();

        verify(tourApiRegionMappingRepository, never()).save(any());
    }
}

package duribun.be.domain.map.controller;

import duribun.be.domain.location.entity.Region;
import duribun.be.domain.location.repository.RegionRepository;
import duribun.be.domain.map.client.TourApiClient;
import duribun.be.domain.map.dto.TourApiDetailCommonResponse;
import duribun.be.domain.map.dto.TourApiDetailImageResponse;
import duribun.be.domain.map.dto.TourApiItemResponse;
import duribun.be.domain.map.entity.TourApiRegionMapping;
import duribun.be.domain.map.repository.TourApiRegionMappingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MapControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private TourApiRegionMappingRepository tourApiRegionMappingRepository;

    @MockitoBean
    private TourApiClient tourApiClient;

    @Test
    void 지역기반_관광지_조회는_인증_없이_200을_반환한다() throws Exception {
        Region region = regionRepository.saveAndFlush(Region.create("51150", "강릉시", 37.7519, 128.8761, 1000));
        tourApiRegionMappingRepository.saveAndFlush(TourApiRegionMapping.create(region.getId(), "32", "1"));
        when(tourApiClient.areaBasedList("32", "1", "12")).thenReturn(List.of(
                new TourApiItemResponse("1", "12", "경포대", "강원 강릉시", "128.90", "37.79", "https://a")));

        mockMvc.perform(get("/api/map/regions/{regionId}/attractions", region.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].contentId").value("1"))
                .andExpect(jsonPath("$[0].title").value("경포대"));
    }

    @Test
    void 존재하지_않는_regionId면_404를_반환한다() throws Exception {
        mockMvc.perform(get("/api/map/regions/{regionId}/attractions", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void 지역코드_매핑이_없으면_200과_빈_배열을_반환한다() throws Exception {
        Region region = regionRepository.saveAndFlush(Region.create("51150", "강릉시", 37.7519, 128.8761, 1000));

        mockMvc.perform(get("/api/map/regions/{regionId}/attractions", region.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void nearby_조회는_인증_없이_200을_반환한다() throws Exception {
        when(tourApiClient.locationBasedList(37.79, 128.9, 5000, "12")).thenReturn(List.of(
                new TourApiItemResponse("1", "12", "경포대", "강원 강릉시", "128.90", "37.79", "https://a")));

        mockMvc.perform(get("/api/map/nearby").param("latitude", "37.79").param("longitude", "128.9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("경포대"));
    }

    @Test
    void search_조회는_인증_없이_200을_반환한다() throws Exception {
        when(tourApiClient.searchKeyword("경포대", "12")).thenReturn(List.of(
                new TourApiItemResponse("1", "12", "경포대", "강원 강릉시", "128.90", "37.79", "https://a")));

        mockMvc.perform(get("/api/map/search").param("keyword", "경포대"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("경포대"));
    }

    @Test
    void 상세조회는_인증_없이_200을_반환한다() throws Exception {
        when(tourApiClient.detailCommon("1")).thenReturn(Optional.of(
                new TourApiDetailCommonResponse("1", "12", "경포대", "강원 강릉시", "128.90", "37.79", "설명", "https://a")));
        when(tourApiClient.detailImage("1")).thenReturn(List.of(new TourApiDetailImageResponse("https://a")));

        mockMvc.perform(get("/api/map/attractions/{contentId}", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("경포대"))
                .andExpect(jsonPath("$.images[0]").value("https://a"));
    }

    @Test
    void 존재하지_않는_contentId면_404를_반환한다() throws Exception {
        when(tourApiClient.detailCommon("no-such-id")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/map/attractions/{contentId}", "no-such-id"))
                .andExpect(status().isNotFound());
    }
}

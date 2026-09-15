package duribun.be.domain.place.controller;

import duribun.be.domain.map.client.TourApiClient;
import duribun.be.domain.map.dto.TourApiItemResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PlaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TourApiClient tourApiClient;

    @Test
    void 장소검색은_인증_없이_200을_반환한다() throws Exception {
        when(tourApiClient.searchKeyword("경복궁", null)).thenReturn(List.of(
                new TourApiItemResponse("1", "12", "경복궁", "서울 종로구 사직로 161", "126.977", "37.579", "https://a")));

        mockMvc.perform(get("/api/places/search").param("keyword", "경복궁"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].placeName").value("경복궁"))
                .andExpect(jsonPath("$[0].address").value("서울 종로구 사직로 161"));
    }

    @Test
    void 검색어가_짧으면_TourAPI_호출_없이_빈_배열을_반환한다() throws Exception {
        mockMvc.perform(get("/api/places/search").param("keyword", "가"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}

package duribun.be.domain.location.controller;

import duribun.be.domain.location.dto.VerifyLocationRequest;
import duribun.be.domain.location.entity.Region;
import duribun.be.domain.location.repository.RegionRepository;
import duribun.be.domain.user.entity.Role;
import duribun.be.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String bearerToken(Long userId) {
        return "Bearer " + jwtTokenProvider.createAccessToken(userId, Role.USER);
    }

    @Test
    void regions_조회는_인증_없이_200을_반환한다() throws Exception {
        regionRepository.saveAndFlush(Region.create("51150", "강릉시", 37.7519, 128.8761, 1000));

        mockMvc.perform(get("/api/locations/regions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("강릉시"));
    }

    @Test
    void verify는_인증되지_않으면_401을_반환한다() throws Exception {
        Region region = regionRepository.saveAndFlush(Region.create("51150", "강릉시", 37.7519, 128.8761, 1000));

        mockMvc.perform(post("/api/locations/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new VerifyLocationRequest(region.getId(), 37.7519, 128.8761))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void visits_조회는_인증되지_않으면_401을_반환한다() throws Exception {
        mockMvc.perform(get("/api/locations/visits"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void verify_반경_이내_좌표로_인증하면_200과_verified_true를_반환한다() throws Exception {
        Region region = regionRepository.saveAndFlush(Region.create("51150", "강릉시", 37.7519, 128.8761, 1000));

        mockMvc.perform(post("/api/locations/verify")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new VerifyLocationRequest(region.getId(), 37.7519, 128.8761))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true))
                .andExpect(jsonPath("$.isFirstVisit").value(true))
                .andExpect(jsonPath("$.regionName").value("강릉시"));
    }

    @Test
    void verify_존재하지_않는_regionId면_404를_반환한다() throws Exception {
        mockMvc.perform(post("/api/locations/verify")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new VerifyLocationRequest(999L, 37.7519, 128.8761))))
                .andExpect(status().isNotFound());
    }

    @Test
    void visits는_로그인한_유저의_방문_목록만_반환한다() throws Exception {
        Region region = regionRepository.saveAndFlush(Region.create("51150", "강릉시", 37.7519, 128.8761, 1000));

        mockMvc.perform(post("/api/locations/verify")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new VerifyLocationRequest(region.getId(), 37.7519, 128.8761))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/locations/verify")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(2L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new VerifyLocationRequest(region.getId(), 37.7519, 128.8761))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/locations/visits")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].regionName").value("강릉시"));
    }
}

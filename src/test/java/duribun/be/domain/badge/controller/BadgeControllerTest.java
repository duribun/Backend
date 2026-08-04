package duribun.be.domain.badge.controller;

import duribun.be.domain.badge.entity.Badge;
import duribun.be.domain.badge.repository.BadgeRepository;
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
class BadgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String bearerToken(Long userId) {
        return "Bearer " + jwtTokenProvider.createAccessToken(userId, Role.USER);
    }

    @Test
    void badges_조회는_인증되지_않으면_401을_반환한다() throws Exception {
        mockMvc.perform(get("/api/badges"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_조회는_인증되지_않으면_401을_반환한다() throws Exception {
        mockMvc.perform(get("/api/badges/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void badges_조회는_아직_획득하지_않은_배지를_acquired_false로_반환한다() throws Exception {
        badgeRepository.saveAndFlush(Badge.create("BEGINNER", "여행 초보자", "설명", 1, null));

        mockMvc.perform(get("/api/badges")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("BEGINNER"))
                .andExpect(jsonPath("$[0].acquired").value(false))
                .andExpect(jsonPath("$[0].acquiredAt").doesNotExist());
    }

    @Test
    void 방문_인증으로_기준을_충족하면_배지가_즉시_acquired_true로_조회된다() throws Exception {
        Region region = regionRepository.saveAndFlush(Region.create("51150", "강릉시", 37.7519, 128.8761, 1000));
        badgeRepository.saveAndFlush(Badge.create("BEGINNER", "여행 초보자", "설명", 1, null));

        mockMvc.perform(post("/api/locations/verify")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new VerifyLocationRequest(region.getId(), 37.7519, 128.8761))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/badges")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("BEGINNER"))
                .andExpect(jsonPath("$[0].acquired").value(true));

        mockMvc.perform(get("/api/badges/me")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].code").value("BEGINNER"));
    }

    @Test
    void me_조회는_보유한_배지만_반환하고_다른_유저의_배지는_섞이지_않는다() throws Exception {
        Region region = regionRepository.saveAndFlush(Region.create("51150", "강릉시", 37.7519, 128.8761, 1000));
        badgeRepository.saveAndFlush(Badge.create("BEGINNER", "여행 초보자", "설명", 1, null));

        mockMvc.perform(post("/api/locations/verify")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new VerifyLocationRequest(region.getId(), 37.7519, 128.8761))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/badges/me")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(2L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}

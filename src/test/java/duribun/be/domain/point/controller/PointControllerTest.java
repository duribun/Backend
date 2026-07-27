package duribun.be.domain.point.controller;

import duribun.be.domain.point.service.PointReason;
import duribun.be.domain.point.service.PointServiceImpl;
import duribun.be.domain.user.entity.Role;
import duribun.be.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PointServiceImpl pointService;

    private String bearerToken(Long userId) {
        return "Bearer " + jwtTokenProvider.createAccessToken(userId, Role.USER);
    }

    @Test
    void me는_인증되지_않으면_401을_반환한다() throws Exception {
        mockMvc.perform(get("/api/points/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void history는_인증되지_않으면_401을_반환한다() throws Exception {
        mockMvc.perform(get("/api/points/history"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me는_로그인한_유저의_잔액을_반환한다() throws Exception {
        pointService.earn(1L, 1500, PointReason.CHARACTER_COLLECT);

        mockMvc.perform(get("/api/points/me")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1500));
    }

    @Test
    void history는_로그인한_유저의_내역만_최신순으로_반환한다() throws Exception {
        pointService.earn(1L, 100, PointReason.CHARACTER_COLLECT);
        pointService.spend(1L, 50, PointReason.SHOP_PURCHASE);
        pointService.earn(2L, 999, PointReason.CHARACTER_COLLECT);

        mockMvc.perform(get("/api/points/history")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].amount").value(-50))
                .andExpect(jsonPath("$.content[1].amount").value(100));
    }
}

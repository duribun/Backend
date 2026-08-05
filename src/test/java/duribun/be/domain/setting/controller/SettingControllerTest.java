package duribun.be.domain.setting.controller;

import duribun.be.domain.auth.dto.SocialUserInfo;
import duribun.be.domain.auth.entity.RefreshToken;
import duribun.be.domain.auth.repository.RefreshTokenRepository;
import duribun.be.domain.user.entity.Role;
import duribun.be.domain.user.entity.SocialProvider;
import duribun.be.domain.user.entity.User;
import duribun.be.domain.user.repository.UserRepository;
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

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SettingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private String bearerToken(Long userId) {
        return "Bearer " + jwtTokenProvider.createAccessToken(userId, Role.USER);
    }

    private Long saveUser(String providerId) {
        User user = userRepository.saveAndFlush(
                User.create(new SocialUserInfo(providerId, "a@a.com", "nick"), SocialProvider.GOOGLE));
        return user.getId();
    }

    @Test
    void me_GET은_인증되지_않으면_401을_반환한다() throws Exception {
        mockMvc.perform(get("/api/settings/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_PATCH는_인증되지_않으면_401을_반환한다() throws Exception {
        mockMvc.perform(patch("/api/settings/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new NotificationBody(false))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void withdraw_인증되지_않으면_401을_반환한다() throws Exception {
        mockMvc.perform(delete("/api/settings/me/withdraw"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_GET은_최초_조회시_기본값을_생성해_반환한다() throws Exception {
        mockMvc.perform(get("/api/settings/me")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationEnabled").value(true));
    }

    @Test
    void me_PATCH는_알림설정을_변경하고_이후_조회에도_반영된다() throws Exception {
        mockMvc.perform(patch("/api/settings/me")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new NotificationBody(false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationEnabled").value(false));

        mockMvc.perform(get("/api/settings/me")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationEnabled").value(false));
    }

    @Test
    void withdraw는_정상_탈퇴시_WITHDRAWN을_응답한다() throws Exception {
        Long userId = saveUser("pid-withdraw-1");

        mockMvc.perform(delete("/api/settings/me/withdraw")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WITHDRAWN"));
    }

    @Test
    void withdraw는_이미_탈퇴한_유저가_다시_요청하면_409를_반환한다() throws Exception {
        Long userId = saveUser("pid-withdraw-2");

        mockMvc.perform(delete("/api/settings/me/withdraw")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(userId)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/settings/me/withdraw")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(userId)))
                .andExpect(status().isConflict());
    }

    @Test
    void withdraw_이후_해당_유저의_RefreshToken이_삭제된다() throws Exception {
        Long userId = saveUser("pid-withdraw-3");
        refreshTokenRepository.saveAndFlush(RefreshToken.create(userId, "existing-refresh-token", LocalDateTime.now().plusDays(14)));

        mockMvc.perform(delete("/api/settings/me/withdraw")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(userId)))
                .andExpect(status().isOk());

        assertThat(refreshTokenRepository.findByToken("existing-refresh-token")).isEmpty();
    }

    private record NotificationBody(boolean notificationEnabled) {
    }
}

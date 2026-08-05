package duribun.be.domain.auth.controller;

import tools.jackson.databind.ObjectMapper;
import duribun.be.domain.auth.client.GoogleAuthClient;
import duribun.be.domain.auth.client.KakaoAuthClient;
import duribun.be.domain.auth.client.NaverAuthClient;
import duribun.be.domain.auth.dto.SocialUserInfo;
import duribun.be.global.exception.InvalidSocialTokenException;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // supports()는 AuthService 생성자에서 Map<SocialProvider, SocialAuthClient>를 구성할 때 쓰이므로
    // 실제 구현(provider 상수 반환)이 그대로 호출되도록 CALLS_REAL_METHODS로 목킹한다.
    @MockitoBean(answers = Answers.CALLS_REAL_METHODS)
    private GoogleAuthClient googleAuthClient;

    @MockitoBean(answers = Answers.CALLS_REAL_METHODS)
    private KakaoAuthClient kakaoAuthClient;

    @MockitoBean(answers = Answers.CALLS_REAL_METHODS)
    private NaverAuthClient naverAuthClient;

    @Test
    void login_google_provider로_로그인하면_200과_토큰을_반환한다() throws Exception {
        doReturn(new SocialUserInfo("google-pid-1", "g@test.com", "google-nick"))
                .when(googleAuthClient).getUserInfo("google-id-token");

        mockMvc.perform(post("/api/auth/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TokenBody("google-id-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.isNewUser").value(true))
                .andExpect(jsonPath("$.userId").isNumber());
    }

    @Test
    void login_동일한_provider_providerId로_재로그인하면_isNewUser가_false다() throws Exception {
        doReturn(new SocialUserInfo("google-pid-2", "g2@test.com", "google-nick2"))
                .when(googleAuthClient).getUserInfo("google-id-token");

        mockMvc.perform(post("/api/auth/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TokenBody("google-id-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isNewUser").value(true));

        mockMvc.perform(post("/api/auth/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TokenBody("google-id-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isNewUser").value(false));
    }

    @Test
    void login_kakao_provider로_로그인하면_200을_반환한다() throws Exception {
        doReturn(new SocialUserInfo("kakao-pid-1", "k@test.com", "kakao-nick"))
                .when(kakaoAuthClient).getUserInfo("kakao-token");

        mockMvc.perform(post("/api/auth/login/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TokenBody("kakao-token"))))
                .andExpect(status().isOk());
    }

    @Test
    void login_naver_provider로_로그인하면_200을_반환한다() throws Exception {
        doReturn(new SocialUserInfo("naver-pid-1", "n@test.com", "naver-nick"))
                .when(naverAuthClient).getUserInfo("naver-token");

        mockMvc.perform(post("/api/auth/login/naver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TokenBody("naver-token"))))
                .andExpect(status().isOk());
    }

    @Test
    void login_소셜_토큰_검증에_실패하면_401을_반환한다() throws Exception {
        doThrow(new InvalidSocialTokenException("소셜 토큰이 유효하지 않습니다"))
                .when(googleAuthClient).getUserInfo("bad-token");

        mockMvc.perform(post("/api/auth/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TokenBody("bad-token"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_지원하지_않는_provider면_400을_반환한다() throws Exception {
        mockMvc.perform(post("/api/auth/login/facebook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TokenBody("some-token"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_token이_비어있으면_400을_반환한다() throws Exception {
        mockMvc.perform(post("/api/auth/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TokenBody(""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reissue_로그인_직후_발급받은_refreshToken으로_재발급에_성공한다() throws Exception {
        doReturn(new SocialUserInfo("google-pid-3", "g3@test.com", "google-nick3"))
                .when(googleAuthClient).getUserInfo("google-id-token");

        String loginResponseJson = mockMvc.perform(post("/api/auth/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TokenBody("google-id-token"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String refreshToken = objectMapper.readTree(loginResponseJson).get("refreshToken").asString();

        mockMvc.perform(post("/api/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshBody(refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").value(refreshToken));
    }

    @Test
    void reissue_유효하지_않은_refreshToken이면_401을_반환한다() throws Exception {
        mockMvc.perform(post("/api/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshBody("not-a-valid-refresh-token"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_유효한_refreshToken이면_204를_반환하고_토큰이_삭제된다() throws Exception {
        doReturn(new SocialUserInfo("google-pid-4", "g4@test.com", "google-nick4"))
                .when(googleAuthClient).getUserInfo("google-id-token");

        String loginResponseJson = mockMvc.perform(post("/api/auth/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TokenBody("google-id-token"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String refreshToken = objectMapper.readTree(loginResponseJson).get("refreshToken").asString();

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshBody(refreshToken))))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshBody(refreshToken))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_존재하지_않는_토큰이어도_204를_반환한다() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshBody("unknown-refresh-token"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void logout_refreshToken이_비어있으면_400을_반환한다() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshBody(""))))
                .andExpect(status().isBadRequest());
    }

    private record TokenBody(String token) {
    }

    private record RefreshBody(String refreshToken) {
    }
}

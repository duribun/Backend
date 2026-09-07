package duribun.be.domain.user.controller;

import duribun.be.domain.auth.dto.SocialUserInfo;
import duribun.be.domain.user.entity.Gender;
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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String bearerToken(Long userId) {
        return "Bearer " + jwtTokenProvider.createAccessToken(userId, Role.USER);
    }

    private Long saveUser(String providerId, String nickname) {
        User user = userRepository.saveAndFlush(
                User.create(new SocialUserInfo(providerId, "a@a.com", nickname), SocialProvider.GOOGLE));
        return user.getId();
    }

    @Test
    void nicknameCheck_인증되지_않으면_401을_반환한다() throws Exception {
        mockMvc.perform(get("/api/users/nickname-check").param("nickname", "여행자"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void profile_PATCH는_인증되지_않으면_401을_반환한다() throws Exception {
        mockMvc.perform(patch("/api/users/me/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProfileBody("여행자", LocalDate.of(2000, 1, 1), Gender.FEMALE))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void nicknameCheck_사용가능한_닉네임이면_available_true를_반환한다() throws Exception {
        Long userId = saveUser("pid-nc-1", "기존닉네임");

        mockMvc.perform(get("/api/users/nickname-check")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(userId))
                        .param("nickname", "새로운닉네임"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void nicknameCheck_다른_유저가_사용중이면_available_false를_반환한다() throws Exception {
        saveUser("pid-nc-2", "선점닉네임");
        Long userId = saveUser("pid-nc-3", "내닉네임");

        mockMvc.perform(get("/api/users/nickname-check")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(userId))
                        .param("nickname", "선점닉네임"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void nicknameCheck_본인이_쓰고있는_닉네임을_재확인하면_available_true를_반환한다() throws Exception {
        Long userId = saveUser("pid-nc-4", "내닉네임2");

        mockMvc.perform(get("/api/users/nickname-check")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(userId))
                        .param("nickname", "내닉네임2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void nicknameCheck_형식이_올바르지_않으면_400을_반환한다() throws Exception {
        Long userId = saveUser("pid-nc-5", "닉네임");

        mockMvc.perform(get("/api/users/nickname-check")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(userId))
                        .param("nickname", "a"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void profile_PATCH는_정상_요청이면_저장하고_200을_반환한다() throws Exception {
        Long userId = saveUser("pid-pf-1", "이전닉네임");

        mockMvc.perform(patch("/api/users/me/profile")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProfileBody("새닉네임", LocalDate.of(2000, 1, 1), Gender.FEMALE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("새닉네임"))
                .andExpect(jsonPath("$.birthDate").value("2000-01-01"))
                .andExpect(jsonPath("$.gender").value("FEMALE"));

        User saved = userRepository.findById(userId).orElseThrow();
        assertThat(saved.getNickname()).isEqualTo("새닉네임");
        assertThat(saved.getBirthDate()).isEqualTo(LocalDate.of(2000, 1, 1));
        assertThat(saved.getGender()).isEqualTo(Gender.FEMALE);
    }

    @Test
    void profile_PATCH는_닉네임_형식이_올바르지_않으면_400을_반환한다() throws Exception {
        Long userId = saveUser("pid-pf-2", "이전닉네임");

        mockMvc.perform(patch("/api/users/me/profile")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProfileBody("a", LocalDate.of(2000, 1, 1), Gender.FEMALE))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void profile_PATCH는_필수값이_누락되면_400을_반환한다() throws Exception {
        Long userId = saveUser("pid-pf-3", "이전닉네임");

        mockMvc.perform(patch("/api/users/me/profile")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"새닉네임\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void profile_PATCH는_다른_유저가_이미_사용중인_닉네임이면_409를_반환한다() throws Exception {
        saveUser("pid-pf-4", "선점닉네임2");
        Long userId = saveUser("pid-pf-5", "내닉네임3");

        mockMvc.perform(patch("/api/users/me/profile")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProfileBody("선점닉네임2", LocalDate.of(2000, 1, 1), Gender.MALE))))
                .andExpect(status().isConflict());
    }

    @Test
    void profile_PATCH는_본인이_기존에_쓰던_닉네임이면_그대로_통과한다() throws Exception {
        Long userId = saveUser("pid-pf-6", "내닉네임4");

        mockMvc.perform(patch("/api/users/me/profile")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProfileBody("내닉네임4", LocalDate.of(2000, 1, 1), Gender.MALE))))
                .andExpect(status().isOk());
    }

    private record ProfileBody(String nickname, LocalDate birthDate, Gender gender) {
    }
}

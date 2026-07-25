package duribun.be.domain.auth.client;

import duribun.be.domain.auth.dto.SocialUserInfo;
import duribun.be.domain.user.entity.SocialProvider;
import duribun.be.global.exception.InvalidSocialTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

class KakaoAuthClientTest {

    private MockRestServiceServer mockServer;
    private KakaoAuthClient kakaoAuthClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        kakaoAuthClient = new KakaoAuthClient(builder);
    }

    @Test
    void 정상_응답이면_SocialUserInfo를_반환한다() {
        mockServer.expect(requestTo("https://kapi.kakao.com/v2/user/me"))
                .andExpect(header("Authorization", "Bearer test-token"))
                .andRespond(withSuccess("""
                        {
                          "id": 12345,
                          "kakao_account": {
                            "email": "kakao@test.com",
                            "profile": { "nickname": "kakao-nick" }
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        SocialUserInfo info = kakaoAuthClient.getUserInfo("test-token");

        assertThat(info.providerId()).isEqualTo("12345");
        assertThat(info.email()).isEqualTo("kakao@test.com");
        assertThat(info.nickname()).isEqualTo("kakao-nick");
    }

    @Test
    void 유효하지_않은_토큰이면_401_응답을_받아_예외를_던진다() {
        mockServer.expect(requestTo("https://kapi.kakao.com/v2/user/me"))
                .andRespond(withUnauthorizedRequest());

        assertThatThrownBy(() -> kakaoAuthClient.getUserInfo("bad-token"))
                .isInstanceOf(InvalidSocialTokenException.class);
    }

    @Test
    void supports는_KAKAO를_반환한다() {
        assertThat(kakaoAuthClient.supports()).isEqualTo(SocialProvider.KAKAO);
    }
}

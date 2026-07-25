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

class NaverAuthClientTest {

    private MockRestServiceServer mockServer;
    private NaverAuthClient naverAuthClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        naverAuthClient = new NaverAuthClient(builder);
    }

    @Test
    void 정상_응답이면_SocialUserInfo를_반환한다() {
        mockServer.expect(requestTo("https://openapi.naver.com/v1/nid/me"))
                .andExpect(header("Authorization", "Bearer test-token"))
                .andRespond(withSuccess("""
                        {
                          "resultcode": "00",
                          "message": "success",
                          "response": {
                            "id": "naver-id-1",
                            "email": "naver@test.com",
                            "nickname": "naver-nick"
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        SocialUserInfo info = naverAuthClient.getUserInfo("test-token");

        assertThat(info.providerId()).isEqualTo("naver-id-1");
        assertThat(info.email()).isEqualTo("naver@test.com");
        assertThat(info.nickname()).isEqualTo("naver-nick");
    }

    @Test
    void 유효하지_않은_토큰이면_401_응답을_받아_예외를_던진다() {
        mockServer.expect(requestTo("https://openapi.naver.com/v1/nid/me"))
                .andRespond(withUnauthorizedRequest());

        assertThatThrownBy(() -> naverAuthClient.getUserInfo("bad-token"))
                .isInstanceOf(InvalidSocialTokenException.class);
    }

    @Test
    void supports는_NAVER를_반환한다() {
        assertThat(naverAuthClient.supports()).isEqualTo(SocialProvider.NAVER);
    }
}

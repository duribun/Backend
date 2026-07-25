package duribun.be.domain.auth.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import duribun.be.domain.auth.dto.SocialUserInfo;
import duribun.be.domain.user.entity.SocialProvider;
import duribun.be.global.exception.InvalidSocialTokenException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class NaverAuthClient implements SocialAuthClient {

    private static final String USER_INFO_URI = "https://openapi.naver.com/v1/nid/me";

    private final RestClient restClient;

    public NaverAuthClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Override
    public SocialUserInfo getUserInfo(String token) {
        NaverUserResponse response;
        try {
            response = restClient.get()
                    .uri(USER_INFO_URI)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(NaverUserResponse.class);
        } catch (RestClientException e) {
            throw new InvalidSocialTokenException("Naver Access Token이 유효하지 않습니다");
        }
        if (response == null || response.response() == null) {
            throw new InvalidSocialTokenException("Naver 사용자 정보 조회에 실패했습니다");
        }
        return response.response().toSocialUserInfo();
    }

    @Override
    public SocialProvider supports() {
        return SocialProvider.NAVER;
    }

    private record NaverUserResponse(
            @JsonProperty("resultcode") String resultCode,
            @JsonProperty("response") NaverAccount response
    ) {
    }

    private record NaverAccount(
            @JsonProperty("id") String id,
            @JsonProperty("email") String email,
            @JsonProperty("nickname") String nickname
    ) {
        SocialUserInfo toSocialUserInfo() {
            return new SocialUserInfo(id, email, nickname);
        }
    }
}

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
public class KakaoAuthClient implements SocialAuthClient {

    private static final String USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";

    private final RestClient restClient;

    public KakaoAuthClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Override
    public SocialUserInfo getUserInfo(String token) {
        KakaoUserResponse response;
        try {
            response = restClient.get()
                    .uri(USER_INFO_URI)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(KakaoUserResponse.class);
        } catch (RestClientException e) {
            throw new InvalidSocialTokenException("Kakao Access Token이 유효하지 않습니다");
        }
        if (response == null) {
            throw new InvalidSocialTokenException("Kakao 사용자 정보 조회에 실패했습니다");
        }
        return response.toSocialUserInfo();
    }

    @Override
    public SocialProvider supports() {
        return SocialProvider.KAKAO;
    }

    private record KakaoUserResponse(
            @JsonProperty("id") Long id,
            @JsonProperty("kakao_account") KakaoAccount kakaoAccount
    ) {
        SocialUserInfo toSocialUserInfo() {
            String email = kakaoAccount != null ? kakaoAccount.email() : null;
            String nickname = kakaoAccount != null && kakaoAccount.profile() != null
                    ? kakaoAccount.profile().nickname()
                    : null;
            return new SocialUserInfo(String.valueOf(id), email, nickname);
        }
    }

    private record KakaoAccount(
            @JsonProperty("email") String email,
            @JsonProperty("profile") KakaoProfile profile
    ) {
    }

    private record KakaoProfile(@JsonProperty("nickname") String nickname) {
    }
}

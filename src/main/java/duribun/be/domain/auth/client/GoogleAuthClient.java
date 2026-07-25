package duribun.be.domain.auth.client;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import duribun.be.domain.auth.dto.SocialUserInfo;
import duribun.be.domain.user.entity.SocialProvider;
import duribun.be.global.exception.InvalidSocialTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Component
public class GoogleAuthClient implements SocialAuthClient {

    private final GoogleIdTokenVerifier verifier;

    public GoogleAuthClient(@Value("${oauth2.google.web-client-id}") String webClientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(List.of(webClientId))
                .build();
    }

    @Override
    public SocialUserInfo getUserInfo(String token) {
        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(token);
        } catch (GeneralSecurityException | IOException | IllegalArgumentException e) {
            throw new InvalidSocialTokenException("Google ID Token 검증 중 오류가 발생했습니다");
        }
        if (idToken == null) {
            throw new InvalidSocialTokenException("Google ID Token이 유효하지 않습니다");
        }
        GoogleIdToken.Payload payload = idToken.getPayload();
        String nickname = (String) payload.get("name");
        return new SocialUserInfo(payload.getSubject(), payload.getEmail(), nickname);
    }

    @Override
    public SocialProvider supports() {
        return SocialProvider.GOOGLE;
    }
}

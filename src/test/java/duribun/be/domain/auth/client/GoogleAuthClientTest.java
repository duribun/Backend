package duribun.be.domain.auth.client;

import duribun.be.domain.user.entity.SocialProvider;
import duribun.be.global.exception.InvalidSocialTokenException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoogleAuthClientTest {

    private final GoogleAuthClient googleAuthClient = new GoogleAuthClient("test-web-client-id");

    @Test
    void 형식이_잘못된_ID_Token이면_예외를_던진다() {
        assertThatThrownBy(() -> googleAuthClient.getUserInfo("not-a-valid-id-token"))
                .isInstanceOf(InvalidSocialTokenException.class);
    }

    @Test
    void supports는_GOOGLE을_반환한다() {
        assertThat(googleAuthClient.supports()).isEqualTo(SocialProvider.GOOGLE);
    }
}

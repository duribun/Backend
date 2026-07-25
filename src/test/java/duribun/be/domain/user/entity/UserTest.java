package duribun.be.domain.user.entity;

import duribun.be.domain.auth.dto.SocialUserInfo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void create_SocialUserInfo와_provider로_기본_USER_role의_User를_생성한다() {
        SocialUserInfo info = new SocialUserInfo("provider-id-123", "user@example.com", "nickname");

        User user = User.create(info, SocialProvider.GOOGLE);

        assertThat(user.getProvider()).isEqualTo(SocialProvider.GOOGLE);
        assertThat(user.getProviderId()).isEqualTo("provider-id-123");
        assertThat(user.getEmail()).isEqualTo("user@example.com");
        assertThat(user.getNickname()).isEqualTo("nickname");
        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.getProfileImageUrl()).isNull();
    }

    @Test
    void create_email과_nickname이_없어도_User를_생성한다() {
        SocialUserInfo info = new SocialUserInfo("provider-id-456", null, null);

        User user = User.create(info, SocialProvider.KAKAO);

        assertThat(user.getEmail()).isNull();
        assertThat(user.getNickname()).isNull();
    }
}

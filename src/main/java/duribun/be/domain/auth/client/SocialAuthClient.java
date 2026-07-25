package duribun.be.domain.auth.client;

import duribun.be.domain.auth.dto.SocialUserInfo;
import duribun.be.domain.user.entity.SocialProvider;

public interface SocialAuthClient {

    SocialUserInfo getUserInfo(String token);

    SocialProvider supports();
}

package duribun.be.domain.user.entity;

import duribun.be.global.exception.UnsupportedProviderException;

public enum SocialProvider {
    GOOGLE,
    KAKAO,
    NAVER;

    public static SocialProvider from(String raw) {
        try {
            return SocialProvider.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new UnsupportedProviderException("지원하지 않는 provider입니다: " + raw);
        }
    }
}

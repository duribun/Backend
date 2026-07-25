package duribun.be.domain.auth.dto;

public record SocialUserInfo(
        String providerId,
        String email,
        String nickname
) {
}

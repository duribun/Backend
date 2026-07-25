package duribun.be.domain.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        boolean isNewUser,
        Long userId
) {
}

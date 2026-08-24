package duribun.be.domain.auth.service;

import duribun.be.domain.auth.client.SocialAuthClient;
import duribun.be.domain.auth.dto.LoginResponse;
import duribun.be.domain.auth.dto.SocialUserInfo;
import duribun.be.domain.auth.dto.TokenResponse;
import duribun.be.domain.auth.entity.RefreshToken;
import duribun.be.domain.auth.repository.RefreshTokenRepository;
import duribun.be.domain.user.entity.SocialProvider;
import duribun.be.domain.user.entity.User;
import duribun.be.domain.user.repository.UserRepository;
import duribun.be.global.exception.InvalidRefreshTokenException;
import duribun.be.global.security.jwt.JwtTokenProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {

    private final Map<SocialProvider, SocialAuthClient> clientsByProvider;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(List<SocialAuthClient> clients,
                        UserRepository userRepository,
                        RefreshTokenRepository refreshTokenRepository,
                        JwtTokenProvider jwtTokenProvider) {
        this.clientsByProvider = clients.stream()
                .collect(Collectors.toUnmodifiableMap(SocialAuthClient::supports, Function.identity()));
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse login(String providerValue, String token) {
        SocialProvider provider = SocialProvider.from(providerValue);
        SocialAuthClient client = clientsByProvider.get(provider);
        SocialUserInfo userInfo = client.getUserInfo(token);

        var existingUser = userRepository.findByProviderAndProviderId(provider, userInfo.providerId());
        boolean isNewUser = existingUser.isEmpty();
        User user = existingUser.orElseGet(() -> userRepository.save(User.create(userInfo, provider)));
        if (user.isWithdrawn()) {
            user.reactivate();
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole());
        String refreshTokenValue = jwtTokenProvider.createRefreshToken(user.getId());
        refreshTokenRepository.save(RefreshToken.create(
                user.getId(), refreshTokenValue, jwtTokenProvider.getExpiration(refreshTokenValue)));

        return new LoginResponse(accessToken, refreshTokenValue, isNewUser, user.getId());
    }

    public TokenResponse reissue(String refreshTokenValue) {
        if (!jwtTokenProvider.isTokenValid(refreshTokenValue) || !jwtTokenProvider.isRefreshToken(refreshTokenValue)) {
            throw new InvalidRefreshTokenException("리프레시 토큰이 유효하지 않습니다");
        }

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(RefreshToken.hash(refreshTokenValue))
                .orElseThrow(() -> new InvalidRefreshTokenException("리프레시 토큰이 유효하지 않습니다"));
        if (storedToken.isExpired()) {
            throw new InvalidRefreshTokenException("리프레시 토큰이 만료되었습니다");
        }

        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new InvalidRefreshTokenException("리프레시 토큰이 유효하지 않습니다"));

        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole());
        return new TokenResponse(newAccessToken, refreshTokenValue);
    }

    public void logout(String refreshTokenValue) {
        refreshTokenRepository.deleteByTokenHash(RefreshToken.hash(refreshTokenValue));
    }

    public void revokeAllTokens(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}

package duribun.be.domain.auth.service;

import duribun.be.domain.auth.client.SocialAuthClient;
import duribun.be.domain.auth.dto.LoginResponse;
import duribun.be.domain.auth.dto.SocialUserInfo;
import duribun.be.domain.auth.dto.TokenResponse;
import duribun.be.domain.auth.entity.RefreshToken;
import duribun.be.domain.auth.repository.RefreshTokenRepository;
import duribun.be.domain.user.entity.Role;
import duribun.be.domain.user.entity.SocialProvider;
import duribun.be.domain.user.entity.User;
import duribun.be.domain.user.repository.UserRepository;
import duribun.be.global.exception.InvalidRefreshTokenException;
import duribun.be.global.exception.InvalidSocialTokenException;
import duribun.be.global.exception.UnsupportedProviderException;
import duribun.be.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private SocialAuthClient googleClient;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        googleClient = mock(SocialAuthClient.class);
        when(googleClient.supports()).thenReturn(SocialProvider.GOOGLE);

        authService = new AuthService(List.of(googleClient), userRepository, refreshTokenRepository, jwtTokenProvider);
    }

    private User userWithId(Long id, SocialProvider provider, String providerId) {
        User user = User.create(new SocialUserInfo(providerId, "a@a.com", "nick"), provider);
        setId(user, id);
        return user;
    }

    private void setId(User user, Long id) {
        try {
            Field field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void login_신규_유저면_User를_생성하고_isNewUser는_true다() {
        when(googleClient.getUserInfo("google-token")).thenReturn(new SocialUserInfo("pid-1", "a@a.com", "nick"));
        when(userRepository.findByProviderAndProviderId(SocialProvider.GOOGLE, "pid-1")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            setId(saved, 100L);
            return saved;
        });
        when(jwtTokenProvider.createAccessToken(100L, Role.USER)).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(100L)).thenReturn("refresh-token");
        when(jwtTokenProvider.getExpiration("refresh-token")).thenReturn(LocalDateTime.now().plusDays(14));

        LoginResponse response = authService.login("google", "google-token");

        assertThat(response.isNewUser()).isTrue();
        assertThat(response.userId()).isEqualTo(100L);
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_동일한_provider_providerId로_재로그인하면_유저를_새로_생성하지_않는다() {
        User existing = userWithId(1L, SocialProvider.GOOGLE, "pid-2");
        when(googleClient.getUserInfo("google-token")).thenReturn(new SocialUserInfo("pid-2", "a@a.com", "nick"));
        when(userRepository.findByProviderAndProviderId(SocialProvider.GOOGLE, "pid-2")).thenReturn(Optional.of(existing));
        when(jwtTokenProvider.createAccessToken(1L, Role.USER)).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(1L)).thenReturn("refresh-token");
        when(jwtTokenProvider.getExpiration("refresh-token")).thenReturn(LocalDateTime.now().plusDays(14));

        LoginResponse response = authService.login("google", "google-token");

        assertThat(response.isNewUser()).isFalse();
        assertThat(response.userId()).isEqualTo(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_탈퇴한_유저가_재로그인하면_status가_ACTIVE로_복구된다() {
        User withdrawnUser = userWithId(1L, SocialProvider.GOOGLE, "pid-withdrawn");
        withdrawnUser.withdraw();
        when(googleClient.getUserInfo("google-token")).thenReturn(new SocialUserInfo("pid-withdrawn", "a@a.com", "nick"));
        when(userRepository.findByProviderAndProviderId(SocialProvider.GOOGLE, "pid-withdrawn")).thenReturn(Optional.of(withdrawnUser));
        when(jwtTokenProvider.createAccessToken(1L, Role.USER)).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(1L)).thenReturn("refresh-token");
        when(jwtTokenProvider.getExpiration("refresh-token")).thenReturn(LocalDateTime.now().plusDays(14));

        authService.login("google", "google-token");

        assertThat(withdrawnUser.isWithdrawn()).isFalse();
        assertThat(withdrawnUser.getWithdrawnAt()).isNull();
    }

    @Test
    void login_지원하지_않는_provider면_예외를_던진다() {
        assertThatThrownBy(() -> authService.login("facebook", "token"))
                .isInstanceOf(UnsupportedProviderException.class);
    }

    @Test
    void login_소셜_토큰_검증에_실패하면_예외가_그대로_전파된다() {
        when(googleClient.getUserInfo("bad-token")).thenThrow(new InvalidSocialTokenException("invalid"));

        assertThatThrownBy(() -> authService.login("google", "bad-token"))
                .isInstanceOf(InvalidSocialTokenException.class);
    }

    @Test
    void reissue_유효한_refreshToken이면_accessToken만_새로_발급하고_refreshToken은_그대로_반환한다() {
        String refreshTokenValue = "valid-refresh-token";
        RefreshToken stored = RefreshToken.create(1L, refreshTokenValue, LocalDateTime.now().plusDays(1));
        User user = userWithId(1L, SocialProvider.GOOGLE, "pid-1");

        when(jwtTokenProvider.isTokenValid(refreshTokenValue)).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken(refreshTokenValue)).thenReturn(true);
        when(refreshTokenRepository.findByTokenHash(RefreshToken.hash(refreshTokenValue))).thenReturn(Optional.of(stored));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.createAccessToken(1L, Role.USER)).thenReturn("new-access-token");

        TokenResponse response = authService.reissue(refreshTokenValue);

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo(refreshTokenValue);
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void reissue_JWT_서명이_유효하지_않으면_예외를_던진다() {
        when(jwtTokenProvider.isTokenValid("bad-token")).thenReturn(false);

        assertThatThrownBy(() -> authService.reissue("bad-token"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void reissue_access_token_타입이면_예외를_던진다() {
        when(jwtTokenProvider.isTokenValid("access-token-used-as-refresh")).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken("access-token-used-as-refresh")).thenReturn(false);

        assertThatThrownBy(() -> authService.reissue("access-token-used-as-refresh"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void reissue_DB에_저장되지_않은_refreshToken이면_예외를_던진다() {
        when(jwtTokenProvider.isTokenValid("unknown-token")).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken("unknown-token")).thenReturn(true);
        when(refreshTokenRepository.findByTokenHash(RefreshToken.hash("unknown-token"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.reissue("unknown-token"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void reissue_만료된_refreshToken이면_예외를_던진다() {
        String refreshTokenValue = "expired-token";
        RefreshToken expired = RefreshToken.create(1L, refreshTokenValue, LocalDateTime.now().minusDays(1));

        when(jwtTokenProvider.isTokenValid(refreshTokenValue)).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken(refreshTokenValue)).thenReturn(true);
        when(refreshTokenRepository.findByTokenHash(RefreshToken.hash(refreshTokenValue))).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> authService.reissue(refreshTokenValue))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void logout_존재하는_토큰이면_삭제한다() {
        authService.logout("some-refresh-token");

        verify(refreshTokenRepository).deleteByTokenHash(RefreshToken.hash("some-refresh-token"));
    }

    @Test
    void logout_존재하지_않는_토큰이어도_예외없이_처리된다() {
        assertThatCode(() -> authService.logout("unknown-token")).doesNotThrowAnyException();

        verify(refreshTokenRepository).deleteByTokenHash(RefreshToken.hash("unknown-token"));
    }

    @Test
    void revokeAllTokens_유저의_모든_리프레시토큰을_삭제한다() {
        authService.revokeAllTokens(1L);

        verify(refreshTokenRepository).deleteByUserId(1L);
    }
}

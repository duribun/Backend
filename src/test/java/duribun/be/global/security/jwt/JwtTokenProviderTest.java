package duribun.be.global.security.jwt;

import duribun.be.domain.user.entity.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class JwtTokenProviderTest {

    private static final String SECRET = "ZHVyaWJ1bi10ZXN0LWp3dC1zZWNyZXQta2V5LWZvci11bml0LXRlc3RzLTMyYnl0ZXM=";

    private final JwtProperties properties = new JwtProperties(SECRET, 60L, 14L);
    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(properties);

    @Test
    void createAccessToken_발급한_토큰에서_userId를_다시_꺼낼_수_있다() {
        String token = jwtTokenProvider.createAccessToken(1L, Role.USER);

        assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(1L);
    }

    @Test
    void createAccessToken은_access_타입_토큰을_만든다() {
        String token = jwtTokenProvider.createAccessToken(1L, Role.USER);

        assertThat(jwtTokenProvider.isRefreshToken(token)).isFalse();
    }

    @Test
    void createRefreshToken은_refresh_타입_토큰을_만든다() {
        String token = jwtTokenProvider.createRefreshToken(1L);

        assertThat(jwtTokenProvider.isRefreshToken(token)).isTrue();
    }

    @Test
    void isTokenValid_정상_발급된_토큰은_true를_반환한다() {
        String token = jwtTokenProvider.createAccessToken(1L, Role.USER);

        assertThat(jwtTokenProvider.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_서명이_변조된_토큰은_false를_반환한다() {
        String token = jwtTokenProvider.createAccessToken(1L, Role.USER);
        String tampered = token.substring(0, token.length() - 1) + (token.endsWith("a") ? "b" : "a");

        assertThat(jwtTokenProvider.isTokenValid(tampered)).isFalse();
    }

    @Test
    void isTokenValid_만료된_토큰은_false를_반환한다() {
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET));
        String expired = Jwts.builder()
                .subject("1")
                .claim("role", Role.USER.name())
                .claim("type", "access")
                .issuedAt(new Date(System.currentTimeMillis() - 120_000))
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(key)
                .compact();

        assertThat(jwtTokenProvider.isTokenValid(expired)).isFalse();
    }

    @Test
    void isTokenValid_형식이_잘못된_토큰은_false를_반환한다() {
        assertThat(jwtTokenProvider.isTokenValid("not-a-jwt")).isFalse();
    }

    @Test
    void getRole_access_token에서_role을_꺼낸다() {
        String token = jwtTokenProvider.createAccessToken(1L, Role.ADMIN);

        assertThat(jwtTokenProvider.getRole(token)).isEqualTo(Role.ADMIN);
    }

    @Test
    void getExpiration_토큰의_만료시각을_반환한다() {
        String token = jwtTokenProvider.createRefreshToken(1L);
        LocalDateTime expected = LocalDateTime.now(java.time.ZoneOffset.UTC).plusDays(14);

        LocalDateTime expiration = jwtTokenProvider.getExpiration(token);

        assertThat(expiration).isCloseTo(expected, within(10, java.time.temporal.ChronoUnit.SECONDS));
    }
}

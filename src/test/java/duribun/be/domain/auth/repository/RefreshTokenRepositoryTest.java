package duribun.be.domain.auth.repository;

import duribun.be.common.config.JpaAuditingConfig;
import duribun.be.domain.auth.entity.RefreshToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void save하면_createdAt_updatedAt이_자동으로_채워진다() {
        RefreshToken refreshToken = RefreshToken.create(1L, "token-value", LocalDateTime.now().plusDays(14));

        RefreshToken saved = refreshTokenRepository.saveAndFlush(refreshToken);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByTokenHash_존재하는_토큰을_조회한다() {
        refreshTokenRepository.saveAndFlush(RefreshToken.create(2L, "find-me", LocalDateTime.now().plusDays(14)));

        Optional<RefreshToken> found = refreshTokenRepository.findByTokenHash(RefreshToken.hash("find-me"));

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(2L);
    }

    @Test
    void findByTokenHash_존재하지_않으면_빈값을_반환한다() {
        Optional<RefreshToken> found = refreshTokenRepository.findByTokenHash(RefreshToken.hash("no-such-token"));

        assertThat(found).isEmpty();
    }
}

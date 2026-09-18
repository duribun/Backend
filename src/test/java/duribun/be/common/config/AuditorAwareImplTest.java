package duribun.be.common.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AuditorAwareImplTest {

    private final AuditorAwareImpl auditorAware = new AuditorAwareImpl();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 인증된_유저의_ID를_반환한다() {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(42L, null, authorities));

        Optional<Long> auditor = auditorAware.getCurrentAuditor();

        assertThat(auditor).contains(42L);
    }

    @Test
    void 인증정보가_없으면_빈값을_반환한다() {
        Optional<Long> auditor = auditorAware.getCurrentAuditor();

        assertThat(auditor).isEmpty();
    }

    @Test
    void 익명_인증이면_빈값을_반환한다() {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"));
        SecurityContextHolder.getContext()
                .setAuthentication(new AnonymousAuthenticationToken("key", "anonymousUser", authorities));

        Optional<Long> auditor = auditorAware.getCurrentAuditor();

        assertThat(auditor).isEmpty();
    }
}

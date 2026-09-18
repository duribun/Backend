package duribun.be.domain.user.repository;

import duribun.be.common.config.JpaAuditingConfig;
import duribun.be.domain.auth.dto.SocialUserInfo;
import duribun.be.domain.user.entity.SocialProvider;
import duribun.be.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void save하면_createdAt_updatedAt이_자동으로_채워진다() {
        User user = User.create(new SocialUserInfo("pid-1", "a@a.com", "nick"), SocialProvider.GOOGLE);

        User saved = userRepository.saveAndFlush(user);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByProviderAndProviderId_존재하는_유저를_조회한다() {
        userRepository.saveAndFlush(User.create(new SocialUserInfo("pid-2", "b@b.com", "nick2"), SocialProvider.KAKAO));

        Optional<User> found = userRepository.findByProviderAndProviderId(SocialProvider.KAKAO, "pid-2");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("b@b.com");
    }

    @Test
    void findByProviderAndProviderId_존재하지_않으면_빈값을_반환한다() {
        Optional<User> found = userRepository.findByProviderAndProviderId(SocialProvider.NAVER, "no-such-id");

        assertThat(found).isEmpty();
    }

    @Test
    void existsByNicknameAndIdNot_다른_유저가_해당_닉네임을_쓰고있으면_true를_반환한다() {
        User other = userRepository.saveAndFlush(User.create(new SocialUserInfo("pid-nick-1", "e@e.com", "겹치는닉네임"), SocialProvider.GOOGLE));

        boolean exists = userRepository.existsByNicknameAndIdNot("겹치는닉네임", other.getId() + 1);

        assertThat(exists).isTrue();
    }

    @Test
    void existsByNicknameAndIdNot_본인의_닉네임이면_false를_반환한다() {
        User self = userRepository.saveAndFlush(User.create(new SocialUserInfo("pid-nick-2", "f@f.com", "내닉네임"), SocialProvider.GOOGLE));

        boolean exists = userRepository.existsByNicknameAndIdNot("내닉네임", self.getId());

        assertThat(exists).isFalse();
    }

    @Test
    void 동일한_provider와_providerId_조합은_유니크_제약을_위반한다() {
        userRepository.saveAndFlush(User.create(new SocialUserInfo("dup-id", "c@c.com", "nick3"), SocialProvider.GOOGLE));

        assertThatThrownBy(() ->
                userRepository.saveAndFlush(User.create(new SocialUserInfo("dup-id", "d@d.com", "nick4"), SocialProvider.GOOGLE))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}

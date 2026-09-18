package duribun.be.domain.user.repository;

import duribun.be.domain.user.entity.SocialProvider;
import duribun.be.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderAndProviderId(SocialProvider provider, String providerId);

    boolean existsByNicknameAndIdNot(String nickname, Long id);
}

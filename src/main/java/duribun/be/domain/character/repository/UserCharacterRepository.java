package duribun.be.domain.character.repository;

import duribun.be.domain.character.entity.UserCharacter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCharacterRepository extends JpaRepository<UserCharacter, Long> {

    boolean existsByUserIdAndCharacterId(Long userId, Long characterId);

    List<UserCharacter> findByUserId(Long userId);
}

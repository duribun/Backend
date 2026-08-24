package duribun.be.domain.character.service;

import duribun.be.domain.character.dto.CharacterResponse;
import duribun.be.domain.character.dto.MyCharacterResponse;
import duribun.be.domain.character.entity.Character;
import duribun.be.domain.character.entity.UserCharacter;
import duribun.be.domain.character.repository.CharacterRepository;
import duribun.be.domain.character.repository.UserCharacterRepository;
import duribun.be.domain.location.event.LocationVerifiedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final UserCharacterRepository userCharacterRepository;

    public CharacterService(CharacterRepository characterRepository,
                             UserCharacterRepository userCharacterRepository) {
        this.characterRepository = characterRepository;
        this.userCharacterRepository = userCharacterRepository;
    }

    @EventListener
    public void handleLocationVerified(LocationVerifiedEvent event) {
        if (!event.isFirstVisit()) {
            return;
        }

        characterRepository.findByRegionId(event.regionId())
                .filter(character -> !userCharacterRepository.existsByUserIdAndCharacterId(event.userId(), character.getId()))
                .ifPresent(character -> grantCharacter(event.userId(), character.getId()));
    }

    public List<CharacterResponse> getAllCharacters(Long userId) {
        List<Character> characters = characterRepository.findAll();
        Map<Long, UserCharacter> acquiredByCharacterId = userCharacterRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(UserCharacter::getCharacterId, Function.identity()));

        return characters.stream()
                .map(character -> CharacterResponse.of(character, acquiredByCharacterId.get(character.getId())))
                .toList();
    }

    public List<MyCharacterResponse> getMyCharacters(Long userId) {
        List<UserCharacter> userCharacters = userCharacterRepository.findByUserId(userId);
        Map<Long, Character> charactersById = characterRepository
                .findAllById(userCharacters.stream().map(UserCharacter::getCharacterId).toList())
                .stream()
                .collect(Collectors.toMap(Character::getId, Function.identity()));

        return userCharacters.stream()
                .map(userCharacter -> MyCharacterResponse.of(charactersById.get(userCharacter.getCharacterId()), userCharacter))
                .toList();
    }

    private void grantCharacter(Long userId, Long characterId) {
        try {
            userCharacterRepository.save(UserCharacter.create(userId, characterId, LocalDateTime.now()));
        } catch (DataIntegrityViolationException e) {
            // 동시 최초 지급 경합: 다른 트랜잭션이 이미 동일 캐릭터를 지급했으므로 무시한다
        }
    }
}

package duribun.be.domain.character.service;

import duribun.be.domain.character.entity.Character;
import duribun.be.domain.character.entity.UserCharacter;
import duribun.be.domain.character.repository.CharacterRepository;
import duribun.be.domain.character.repository.UserCharacterRepository;
import duribun.be.domain.location.event.LocationVerifiedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CharacterServiceTest {

    @Mock
    private CharacterRepository characterRepository;
    @Mock
    private UserCharacterRepository userCharacterRepository;

    private CharacterService characterService;

    @BeforeEach
    void setUp() {
        characterService = new CharacterService(characterRepository, userCharacterRepository);
    }

    private Character characterWithId(Long id, Long regionId, String name) {
        Character character = Character.create(regionId, name, name, null);
        setId(character, id);
        return character;
    }

    private void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void handleLocationVerified_재방문이면_아무것도_하지_않는다() {
        characterService.handleLocationVerified(new LocationVerifiedEvent(10L, 1L, false));

        verifyNoInteractions(characterRepository);
        verifyNoInteractions(userCharacterRepository);
    }

    @Test
    void handleLocationVerified_최초방문이고_매핑된_캐릭터가_있으면_지급한다() {
        Character character = characterWithId(1L, 5L, "강릉이");
        when(characterRepository.findByRegionId(5L)).thenReturn(Optional.of(character));
        when(userCharacterRepository.existsByUserIdAndCharacterId(10L, 1L)).thenReturn(false);

        characterService.handleLocationVerified(new LocationVerifiedEvent(10L, 5L, true));

        ArgumentCaptor<UserCharacter> captor = ArgumentCaptor.forClass(UserCharacter.class);
        verify(userCharacterRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(10L);
        assertThat(captor.getValue().getCharacterId()).isEqualTo(1L);
    }

    @Test
    void handleLocationVerified_이미_보유한_캐릭터는_다시_지급하지_않는다() {
        Character character = characterWithId(1L, 5L, "강릉이");
        when(characterRepository.findByRegionId(5L)).thenReturn(Optional.of(character));
        when(userCharacterRepository.existsByUserIdAndCharacterId(10L, 1L)).thenReturn(true);

        characterService.handleLocationVerified(new LocationVerifiedEvent(10L, 5L, true));

        verify(userCharacterRepository, never()).save(any());
    }

    @Test
    void handleLocationVerified_매핑된_캐릭터가_없으면_조용히_무시한다() {
        when(characterRepository.findByRegionId(5L)).thenReturn(Optional.empty());

        characterService.handleLocationVerified(new LocationVerifiedEvent(10L, 5L, true));

        verify(userCharacterRepository, never()).save(any());
    }

    @Test
    void getAllCharacters_전체_캐릭터에_보유여부를_포함해서_반환한다() {
        Character owned = characterWithId(1L, 5L, "강릉이");
        Character notOwned = characterWithId(2L, 6L, "서울이");
        UserCharacter acquired = UserCharacter.create(10L, 1L, LocalDateTime.now());
        when(characterRepository.findAll()).thenReturn(List.of(owned, notOwned));
        when(userCharacterRepository.findByUserId(10L)).thenReturn(List.of(acquired));

        var responses = characterService.getAllCharacters(10L);

        assertThat(responses).hasSize(2);
        assertThat(responses).filteredOn(r -> r.characterId().equals(1L))
                .allMatch(r -> r.acquired());
        assertThat(responses).filteredOn(r -> r.characterId().equals(2L))
                .allMatch(r -> !r.acquired());
    }

    @Test
    void getMyCharacters_보유한_캐릭터만_반환한다() {
        Character character = characterWithId(1L, 5L, "강릉이");
        UserCharacter acquired = UserCharacter.create(10L, 1L, LocalDateTime.now());
        when(userCharacterRepository.findByUserId(10L)).thenReturn(List.of(acquired));
        when(characterRepository.findAllById(List.of(1L))).thenReturn(List.of(character));

        var responses = characterService.getMyCharacters(10L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).characterId()).isEqualTo(1L);
        assertThat(responses.get(0).name()).isEqualTo("강릉이");
    }
}

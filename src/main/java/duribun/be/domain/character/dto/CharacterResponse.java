package duribun.be.domain.character.dto;

import duribun.be.domain.character.entity.Character;
import duribun.be.domain.character.entity.UserCharacter;

import java.time.LocalDateTime;

public record CharacterResponse(
        Long characterId,
        Long regionId,
        String name,
        String imageUrl,
        boolean acquired,
        LocalDateTime acquiredAt
) {

    public static CharacterResponse of(Character character, UserCharacter userCharacter) {
        boolean acquired = userCharacter != null;
        return new CharacterResponse(
                character.getId(),
                character.getRegionId(),
                character.getName(),
                character.getImageUrl(),
                acquired,
                acquired ? userCharacter.getAcquiredAt() : null
        );
    }
}

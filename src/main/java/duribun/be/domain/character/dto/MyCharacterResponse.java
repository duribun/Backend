package duribun.be.domain.character.dto;

import duribun.be.domain.character.entity.Character;
import duribun.be.domain.character.entity.UserCharacter;

import java.time.LocalDateTime;

public record MyCharacterResponse(
        Long characterId,
        String name,
        String imageUrl,
        LocalDateTime acquiredAt
) {

    public static MyCharacterResponse of(Character character, UserCharacter userCharacter) {
        return new MyCharacterResponse(
                character.getId(),
                character.getName(),
                character.getImageUrl(),
                userCharacter.getAcquiredAt()
        );
    }
}

package duribun.be.domain.character.controller;

import duribun.be.domain.character.dto.CharacterResponse;
import duribun.be.domain.character.dto.MyCharacterResponse;
import duribun.be.domain.character.service.CharacterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/characters")
public class CharacterController {

    private final CharacterService characterService;

    public CharacterController(CharacterService characterService) {
        this.characterService = characterService;
    }

    @GetMapping
    public ResponseEntity<List<CharacterResponse>> getCharacters(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(characterService.getAllCharacters(userId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<MyCharacterResponse>> getMyCharacters(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(characterService.getMyCharacters(userId));
    }
}

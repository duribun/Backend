package duribun.be.domain.character.config;

import duribun.be.domain.character.entity.Character;
import duribun.be.domain.character.repository.CharacterRepository;
import duribun.be.domain.location.repository.RegionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

@Configuration
public class CharacterSeedConfig {

    @Bean
    @Profile("dev")
    @Order(2)
    public CommandLineRunner characterSeedRunner(CharacterRepository characterRepository,
                                                  RegionRepository regionRepository) {
        // RegionSeedConfig가 시드하는 강릉시/서울특별시에 대응하는 테스트용 캐릭터만 우선 시드
        return args -> {
            if (characterRepository.count() > 0) {
                return;
            }
            regionRepository.findBySigunguCode("51150") // 강릉시
                    .ifPresent(region -> characterRepository.save(
                            Character.create(region.getId(), "강릉이", "강릉을 사랑하는 캐릭터", null)));
            regionRepository.findBySigunguCode("11000") // 서울특별시
                    .ifPresent(region -> characterRepository.save(
                            Character.create(region.getId(), "서울이", "서울을 사랑하는 캐릭터", null)));
        };
    }
}

package duribun.be.domain.mascot.config;

import duribun.be.domain.mascot.entity.Mascot;
import duribun.be.domain.mascot.repository.MascotRepository;
import duribun.be.domain.location.repository.RegionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

@Configuration
public class MascotSeedConfig {

    @Bean
    @Profile("dev")
    @Order(2)
    public CommandLineRunner mascotSeedRunner(MascotRepository mascotRepository,
                                               RegionRepository regionRepository) {
        // RegionSeedConfig가 시드하는 강릉시/서울특별시에 대응하는 테스트용 마스코트만 우선 시드
        return args -> {
            if (mascotRepository.count() > 0) {
                return;
            }
            regionRepository.findBySigunguCode("51150") // 강릉시
                    .ifPresent(region -> mascotRepository.save(
                            Mascot.create(region.getId(), "강릉이", "강릉을 사랑하는 마스코트", null)));
            regionRepository.findBySigunguCode("11000") // 서울특별시
                    .ifPresent(region -> mascotRepository.save(
                            Mascot.create(region.getId(), "서울이", "서울을 사랑하는 마스코트", null)));
        };
    }
}

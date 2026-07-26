package duribun.be.domain.location.config;

import duribun.be.domain.location.entity.Region;
import duribun.be.domain.location.repository.RegionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class RegionSeedConfig {

    @Bean
    @Profile("dev")
    public CommandLineRunner regionSeedRunner(RegionRepository regionRepository) {
        // 일단 API를 받아오는 것도 X 예시 데이터인 서울 및 강릉만
        return args -> {
            if (regionRepository.count() > 0) {
                return;
            }
            regionRepository.save(Region.create("51150", "강릉시", 37.7519, 128.8761, 1000));
            regionRepository.save(Region.create("11000", "서울특별시", 37.5665, 126.9780, 1000));
        };
    }
}

package duribun.be.domain.badge.config;

import duribun.be.domain.badge.entity.Badge;
import duribun.be.domain.badge.repository.BadgeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class BadgeSeedConfig {

    @Bean
    @Profile("dev")
    public CommandLineRunner badgeSeedRunner(BadgeRepository badgeRepository) {
        // 예시 기준값 - 실제 서비스 밸런스는 팀 논의 후 확정 (docs/badge.md 참고)
        return args -> {
            if (badgeRepository.count() > 0) {
                return;
            }
            badgeRepository.save(Badge.create("BEGINNER", "여행 초보자", "지역 1곳을 방문 인증했어요", 1, null));
            badgeRepository.save(Badge.create("EXPLORER", "국내 탐험가", "지역 5곳을 방문 인증했어요", 5, null));
            badgeRepository.save(Badge.create("MASTER", "여행 마스터", "지역 10곳을 방문 인증했어요", 10, null));
        };
    }
}

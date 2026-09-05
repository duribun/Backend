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
        // 기획 확정 10단계 기준값 (docs/badge.md 5번 참고)
        return args -> {
            if (badgeRepository.count() > 0) {
                return;
            }
            badgeRepository.save(Badge.create("SEEDLING", "여행 새싹", "마스코트 여행을 시작했어요", 0, null));
            badgeRepository.save(Badge.create("BEGINNER", "여행 입문자", "마스코트 5개를 모았어요", 5, null));
            badgeRepository.save(Badge.create("NOVICE_EXPLORER", "초보 탐험가", "마스코트 10개를 모았어요", 10, null));
            badgeRepository.save(Badge.create("REGION_COLLECTOR", "지역 수집가", "마스코트 15개를 모았어요", 15, null));
            badgeRepository.save(Badge.create("PIONEER", "여행 개척자", "마스코트 20개를 모았어요", 20, null));
            badgeRepository.save(Badge.create("EXPERT", "여행 전문가", "마스코트 25개를 모았어요", 25, null));
            badgeRepository.save(Badge.create("VETERAN", "베테랑 여행자", "마스코트 30개를 모았어요", 30, null));
            badgeRepository.save(Badge.create("NATIONWIDE_TRAVELER", "전국 여행가", "마스코트 45개를 모았어요", 45, null));
            badgeRepository.save(Badge.create("MASTER_COLLECTOR", "마스터 컬렉터", "마스코트 50개를 모았어요", 50, null));
            badgeRepository.save(Badge.create("LEGEND", "레전드 탐험가", "마스코트 55개 이상을 모았어요", 55, null));
        };
    }
}

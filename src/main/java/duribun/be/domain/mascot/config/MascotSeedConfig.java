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
        // docs/ISSUE-지역마스코트시드-실데이터교체.md — RegionSeedConfig가 시드하는 10개 실제 지역에
        // 대응하는 마스코트로 교체(기존 강릉이/서울이는 테스트용). description은 Figma "(3) 위치 인증"
        // 플로우(node 312:133)의 소개 문구를 그대로 옮겼다.
        //
        // imageUrl은 계속 null이다 — FE 쪽(docs/ISSUE-마스코트도감-로컬에셋매핑.md)이 이미지를 원격
        // URL이 아니라 마스코트 이름 기준 로컬 번들 에셋으로 매핑하는 방식으로 가기로 했기 때문에,
        // BE가 이미지를 호스팅할 필요가 없다.
        return args -> {
            if (mascotRepository.count() > 0) {
                return;
            }
            regionRepository.findBySigunguCode("47431") // 독도
                    .ifPresent(region -> mascotRepository.save(
                            Mascot.create(region.getId(), "강치", "독도의 경비대 강치! 우리를 안전하게 지켜줘요.", null)));
            regionRepository.findBySigunguCode("11000") // 서울특별시
                    .ifPresent(region -> mascotRepository.save(
                            Mascot.create(region.getId(), "해치", "서울의 수호자 해치! 언제나 씩씩하게 서울을 지켜줘요.", null)));
            regionRepository.findBySigunguCode("31000") // 울산광역시
                    .ifPresent(region -> mascotRepository.save(
                            Mascot.create(region.getId(), "고래", "울산 앞바다의 고래! 시원한 바다 여행을 함께해요.", null)));
            regionRepository.findBySigunguCode("47430") // 울릉군
                    .ifPresent(region -> mascotRepository.save(
                            Mascot.create(region.getId(), "괭이갈매기", "섬과 바다의 친구 괭이갈매기! 울릉도 곳곳을 자유롭게 날아다녀요.", null)));
            regionRepository.findBySigunguCode("26000") // 부산광역시
                    .ifPresent(region -> mascotRepository.save(
                            Mascot.create(region.getId(), "동백", "차가운 겨울에 피는 부산의 동백! 따뜻한 마음을 전해줘요.", null)));
            regionRepository.findBySigunguCode("30000") // 대전광역시
                    .ifPresent(region -> mascotRepository.save(
                            Mascot.create(region.getId(), "빵", "빵의 도시 대전! 고소한 향기로 여행자를 이끌어요.", null)));
            regionRepository.findBySigunguCode("27000") // 대구광역시
                    .ifPresent(region -> mascotRepository.save(
                            Mascot.create(region.getId(), "사과", "햇살 가득 머금은 대구 사과! 상큼한 맛으로 여행에 활력을 더해줘요", null)));
            regionRepository.findBySigunguCode("29000") // 광주광역시
                    .ifPresent(region -> mascotRepository.save(
                            Mascot.create(region.getId(), "철쭉", "봄을 알리는 무등산의 철쭉! 멋진 꽃길로 여행자를 반겨줘요.", null)));
            regionRepository.findBySigunguCode("28000") // 인천광역시
                    .ifPresent(region -> mascotRepository.save(
                            Mascot.create(region.getId(), "학", "인천의 자연을 품은 학! 갯벌과 바다를 자유롭게 여행해요.", null)));
            regionRepository.findBySigunguCode("50000") // 제주특별자치도
                    .ifPresent(region -> mascotRepository.save(
                            Mascot.create(region.getId(), "노루", "제주 숲속의 귀여운 노루! 자연과 함께 즐거운 여행을 떠나요.", null)));
        };
    }
}

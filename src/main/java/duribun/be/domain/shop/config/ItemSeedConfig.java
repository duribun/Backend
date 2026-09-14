package duribun.be.domain.shop.config;

import duribun.be.domain.shop.entity.Item;
import duribun.be.domain.shop.entity.ItemCategory;
import duribun.be.domain.shop.repository.ItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class ItemSeedConfig {

    @Bean
    @Profile("dev")
    public CommandLineRunner itemSeedRunner(ItemRepository itemRepository) {
        return args -> {
            if (itemRepository.count() > 0) {
                return;
            }
            // TOP
            itemRepository.save(Item.create("그린 라운드넥 티셔츠", "산뜻한 그린 라운드넥 티셔츠", 350, null, ItemCategory.TOP));
            itemRepository.save(Item.create("스트라이프 셔츠", "캐주얼한 스트라이프 셔츠", 450, null, ItemCategory.TOP));
            itemRepository.save(Item.create("옐로우 후드티", "포근한 옐로우 후드티", 550, null, ItemCategory.TOP));
            itemRepository.save(Item.create("블루 후리스 자켓", "따뜻한 블루 후리스 자켓", 700, null, ItemCategory.TOP));
            itemRepository.save(Item.create("그린 조끼", "실용적인 그린 조끼", 650, null, ItemCategory.TOP));
            // BOTTOM
            itemRepository.save(Item.create("데님 반바지", "활동적인 데님 반바지", 350, null, ItemCategory.BOTTOM));
            itemRepository.save(Item.create("카고 팬츠", "튼튼한 카고 팬츠", 500, null, ItemCategory.BOTTOM));
            // HAT
            itemRepository.save(Item.create("밀짚모자", "여름 여행 필수 밀짚모자", 300, null, ItemCategory.HAT));
            itemRepository.save(Item.create("등산 모자", "자외선 차단 등산 모자", 400, null, ItemCategory.HAT));
            // SHOES
            itemRepository.save(Item.create("캔버스 스니커즈", "편안한 캔버스 스니커즈", 550, null, ItemCategory.SHOES));
            itemRepository.save(Item.create("샌들", "시원한 여름용 샌들", 400, null, ItemCategory.SHOES));
            // ACCESSORY (기존 여행소품: 선글라스/배낭/캐리어)
            itemRepository.save(Item.create("여행자 선글라스", "세련된 여행자 선글라스", 500, null, ItemCategory.ACCESSORY));
            itemRepository.save(Item.create("파일럿 선글라스", "멋진 파일럿 스타일 선글라스", 800, null, ItemCategory.ACCESSORY));
            itemRepository.save(Item.create("등산 배낭", "튼튼한 등산용 배낭", 600, null, ItemCategory.ACCESSORY));
            itemRepository.save(Item.create("여행용 크로스백", "가벼운 여행용 크로스백", 400, null, ItemCategory.ACCESSORY));
            itemRepository.save(Item.create("캐리어 20인치", "여행 필수품 캐리어", 1000, null, ItemCategory.ACCESSORY));
            itemRepository.save(Item.create("캐리어 24인치", "넉넉한 장기 여행용 캐리어", 1200, null, ItemCategory.ACCESSORY));
        };
    }
}

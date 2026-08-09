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
            itemRepository.save(Item.create("여행자 선글라스", "세련된 여행자 선글라스", 500, null, ItemCategory.GLASSES));
            itemRepository.save(Item.create("파일럿 선글라스", "멋진 파일럿 스타일 선글라스", 800, null, ItemCategory.GLASSES));
            itemRepository.save(Item.create("등산 배낭", "튼튼한 등산용 배낭", 600, null, ItemCategory.BAG));
            itemRepository.save(Item.create("여행용 크로스백", "가벼운 여행용 크로스백", 400, null, ItemCategory.BAG));
            itemRepository.save(Item.create("캐리어 20인치", "여행 필수품 캐리어", 1000, null, ItemCategory.CARRIER));
            itemRepository.save(Item.create("캐리어 24인치", "넉넉한 장기 여행용 캐리어", 1200, null, ItemCategory.CARRIER));
            itemRepository.save(Item.create("밀짚모자", "여름 여행 필수 밀짚모자", 300, null, ItemCategory.HAT));
            itemRepository.save(Item.create("등산 모자", "자외선 차단 등산 모자", 400, null, ItemCategory.HAT));
        };
    }
}

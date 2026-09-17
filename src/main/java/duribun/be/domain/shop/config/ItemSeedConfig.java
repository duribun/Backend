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

    // Figma "(6) 상점" 프레임에서 export한 이미지. S3 items/ 경로에 업로드된 파일명을 그대로 참조함(파일명이 DB id와 반드시 일치하진 않음).
    private static final String ITEM_IMAGE_BASE_URL =
            "https://duribun-s3-bucket-678191059624-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/items/";

    @Bean
    @Profile("dev")
    public CommandLineRunner itemSeedRunner(ItemRepository itemRepository) {
        return args -> {
            if (itemRepository.count() > 0) {
                return;
            }
            // TOP (Figma 그룹명/가격 그대로 반영 — 기존 이름은 실제 레이어명과 달랐음)
            itemRepository.save(Item.create("초록 상의", "산뜻한 초록 상의", 30, ITEM_IMAGE_BASE_URL + "1.png", ItemCategory.TOP));
            itemRepository.save(Item.create("파란 셔츠", "캐주얼한 파란 셔츠", 40, ITEM_IMAGE_BASE_URL + "2.png", ItemCategory.TOP));
            itemRepository.save(Item.create("노란 상의", "포근한 노란 상의", 40, ITEM_IMAGE_BASE_URL + "3.png", ItemCategory.TOP));
            itemRepository.save(Item.create("파란 후리스", "따뜻한 파란 후리스", 70, ITEM_IMAGE_BASE_URL + "4.png", ItemCategory.TOP));
            itemRepository.save(Item.create("초록 후리스", "포근한 초록 후리스", 70, ITEM_IMAGE_BASE_URL + "5.png", ItemCategory.TOP));
            // BOTTOM (Figma 상점 하의 탭 5종 전부 반영, 가격도 Figma 표기 그대로)
            itemRepository.save(Item.create("파란 반바지", "시원한 파란 반바지", 30, ITEM_IMAGE_BASE_URL + "bottom-blue-shorts.png", ItemCategory.BOTTOM));
            itemRepository.save(Item.create("초록 반바지", "산뜻한 초록 반바지", 40, ITEM_IMAGE_BASE_URL + "bottom-green-shorts.png", ItemCategory.BOTTOM));
            itemRepository.save(Item.create("노란 반바지", "발랄한 노란 반바지", 40, ITEM_IMAGE_BASE_URL + "bottom-yellow-shorts.png", ItemCategory.BOTTOM));
            itemRepository.save(Item.create("파란 긴바지", "캐주얼한 파란 긴바지", 70, ITEM_IMAGE_BASE_URL + "bottom-blue-long-pants.png", ItemCategory.BOTTOM));
            itemRepository.save(Item.create("초록 긴바지", "편안한 초록 긴바지", 70, ITEM_IMAGE_BASE_URL + "bottom-green-long-pants.png", ItemCategory.BOTTOM));
            // SHOES (Figma 상점 신발 탭 6종 전부 반영, 가격도 Figma 표기 그대로)
            itemRepository.save(Item.create("초록 신발", "발랄한 초록 신발", 30, ITEM_IMAGE_BASE_URL + "shoes-green.png", ItemCategory.SHOES));
            itemRepository.save(Item.create("갈색 신발", "클래식한 갈색 신발", 40, ITEM_IMAGE_BASE_URL + "shoes-brown-sneaker.png", ItemCategory.SHOES));
            itemRepository.save(Item.create("노란 신발", "포인트가 되는 노란 신발", 40, ITEM_IMAGE_BASE_URL + "shoes-yellow.png", ItemCategory.SHOES));
            itemRepository.save(Item.create("흰색 신발", "깔끔한 흰색 신발", 40, ITEM_IMAGE_BASE_URL + "shoes-white.png", ItemCategory.SHOES));
            itemRepository.save(Item.create("등산 신발", "험한 길도 거뜬한 등산 신발", 70, ITEM_IMAGE_BASE_URL + "shoes-hiking-boots.png", ItemCategory.SHOES));
            itemRepository.save(Item.create("초록 샌들 신발", "시원한 초록 샌들", 70, ITEM_IMAGE_BASE_URL + "shoes-green-sandal.png", ItemCategory.SHOES));
            // HAT/ACCESSORY: Figma "(6) 상점" 목업에 해당 탭 아이템 이미지가 아예 없어(디자인 미착수) 시드에서 제외.
            // 기존 8개는 V12 마이그레이션으로 삭제했고, ItemCategory enum 값 자체는 이후 디자인 확정 시 재사용을 위해 유지한다.
        };
    }
}

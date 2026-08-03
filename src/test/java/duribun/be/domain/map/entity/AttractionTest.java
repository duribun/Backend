package duribun.be.domain.map.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AttractionTest {

    @Test
    void create하면_필드가_채워지고_syncedAt이_현재시각으로_설정된다() {
        Attraction attraction = Attraction.create("12345", 1L, "12", "경포대", "강원 강릉시",
                37.79, 128.90, "설명", "https://image");

        assertThat(attraction.getContentId()).isEqualTo("12345");
        assertThat(attraction.getRegionId()).isEqualTo(1L);
        assertThat(attraction.getTitle()).isEqualTo("경포대");
        assertThat(attraction.getSyncedAt()).isNotNull();
    }

    @Test
    void isStale_syncedAt이_기준시각보다_이전이면_true를_반환한다() {
        Attraction attraction = Attraction.create("12345", 1L, "12", "경포대", "강원 강릉시",
                37.79, 128.90, null, null);

        boolean stale = attraction.isStale(LocalDateTime.now().plusHours(1));

        assertThat(stale).isTrue();
    }

    @Test
    void isStale_syncedAt이_기준시각보다_이후이면_false를_반환한다() {
        Attraction attraction = Attraction.create("12345", 1L, "12", "경포대", "강원 강릉시",
                37.79, 128.90, null, null);

        boolean stale = attraction.isStale(LocalDateTime.now().minusHours(1));

        assertThat(stale).isFalse();
    }

    @Test
    void updateFromTourApi_regionId는_변경하지_않고_나머지_필드와_syncedAt을_갱신한다() throws InterruptedException {
        Attraction attraction = Attraction.create("12345", 1L, "12", "옛이름", "옛주소",
                1.0, 1.0, null, null);
        LocalDateTime beforeUpdate = attraction.getSyncedAt();
        Thread.sleep(5);

        attraction.updateFromTourApi("새이름", "새주소", 2.0, 2.0, "새설명", "https://new-image");

        assertThat(attraction.getRegionId()).isEqualTo(1L);
        assertThat(attraction.getTitle()).isEqualTo("새이름");
        assertThat(attraction.getAddress()).isEqualTo("새주소");
        assertThat(attraction.getDescription()).isEqualTo("새설명");
        assertThat(attraction.getImageUrl()).isEqualTo("https://new-image");
        assertThat(attraction.getSyncedAt()).isAfter(beforeUpdate);
    }

    @Test
    void assignRegion_regionId를_변경한다() {
        Attraction attraction = Attraction.create("12345", null, "12", "경포대", "강원 강릉시",
                37.79, 128.90, null, null);

        attraction.assignRegion(5L);

        assertThat(attraction.getRegionId()).isEqualTo(5L);
    }
}

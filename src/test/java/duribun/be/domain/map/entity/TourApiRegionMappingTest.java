package duribun.be.domain.map.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TourApiRegionMappingTest {

    @Test
    void create하면_필드가_채워진다() {
        TourApiRegionMapping mapping = TourApiRegionMapping.create(1L, "32", "1");

        assertThat(mapping.getRegionId()).isEqualTo(1L);
        assertThat(mapping.getTourApiAreaCode()).isEqualTo("32");
        assertThat(mapping.getTourApiSigunguCode()).isEqualTo("1");
    }

    @Test
    void create_시군구코드는_null일_수_있다() {
        TourApiRegionMapping mapping = TourApiRegionMapping.create(1L, "1", null);

        assertThat(mapping.getTourApiSigunguCode()).isNull();
    }
}

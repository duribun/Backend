package duribun.be.domain.map.entity;

import duribun.be.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tour_api_region_mappings", uniqueConstraints = @UniqueConstraint(columnNames = "region_id"))
public class TourApiRegionMapping extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "region_id", nullable = false)
    private Long regionId;

    @Column(name = "tour_api_area_code", nullable = false)
    private String tourApiAreaCode;

    @Column(name = "tour_api_sigungu_code")
    private String tourApiSigunguCode;

    private TourApiRegionMapping(Long regionId, String tourApiAreaCode, String tourApiSigunguCode) {
        this.regionId = regionId;
        this.tourApiAreaCode = tourApiAreaCode;
        this.tourApiSigunguCode = tourApiSigunguCode;
    }

    public static TourApiRegionMapping create(Long regionId, String tourApiAreaCode, String tourApiSigunguCode) {
        return new TourApiRegionMapping(regionId, tourApiAreaCode, tourApiSigunguCode);
    }
}

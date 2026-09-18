package duribun.be.domain.location.entity;

import duribun.be.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "regions")
public class Region extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sigungu_code", nullable = false)
    private String sigunguCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Integer verificationRadiusMeters;

    private Region(String sigunguCode, String name, Double latitude, Double longitude, Integer verificationRadiusMeters) {
        this.sigunguCode = sigunguCode;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.verificationRadiusMeters = verificationRadiusMeters;
    }

    public static Region create(String sigunguCode, String name, double latitude, double longitude, int verificationRadiusMeters) {
        return new Region(sigunguCode, name, latitude, longitude, verificationRadiusMeters);
    }
}

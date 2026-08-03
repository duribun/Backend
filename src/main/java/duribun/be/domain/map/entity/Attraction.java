package duribun.be.domain.map.entity;

import duribun.be.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "attractions", uniqueConstraints = @UniqueConstraint(columnNames = "content_id"))
public class Attraction extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_id", nullable = false)
    private String contentId;

    @Column(name = "region_id")
    private Long regionId;

    @Column(name = "content_type_id", nullable = false)
    private String contentTypeId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;

    private Attraction(String contentId, Long regionId, String contentTypeId, String title, String address,
                        Double latitude, Double longitude, String description, String imageUrl) {
        this.contentId = contentId;
        this.regionId = regionId;
        this.contentTypeId = contentTypeId;
        this.title = title;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.imageUrl = imageUrl;
        this.syncedAt = LocalDateTime.now();
    }

    public static Attraction create(String contentId, Long regionId, String contentTypeId, String title,
                                     String address, Double latitude, Double longitude, String description,
                                     String imageUrl) {
        return new Attraction(contentId, regionId, contentTypeId, title, address, latitude, longitude, description,
                imageUrl);
    }

    public boolean isStale(LocalDateTime threshold) {
        return syncedAt.isBefore(threshold);
    }

    public void updateFromTourApi(String title, String address, Double latitude, Double longitude,
                                   String description, String imageUrl) {
        this.title = title;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.imageUrl = imageUrl;
        this.syncedAt = LocalDateTime.now();
    }

    public void assignRegion(Long regionId) {
        this.regionId = regionId;
    }
}

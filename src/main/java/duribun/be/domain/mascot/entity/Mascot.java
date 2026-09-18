package duribun.be.domain.mascot.entity;

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
@Table(name = "mascots", uniqueConstraints = @UniqueConstraint(columnNames = "region_id"))
public class Mascot extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "region_id", nullable = false)
    private Long regionId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    private Mascot(Long regionId, String name, String description, String imageUrl) {
        this.regionId = regionId;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public static Mascot create(Long regionId, String name, String description, String imageUrl) {
        return new Mascot(regionId, name, description, imageUrl);
    }
}

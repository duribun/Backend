package duribun.be.domain.badge.entity;

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
@Table(name = "badges", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
public class Badge extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "required_mascot_count", nullable = false)
    private Integer requiredMascotCount;

    @Column(name = "icon_url")
    private String iconUrl;

    private Badge(String code, String name, String description, Integer requiredMascotCount, String iconUrl) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.requiredMascotCount = requiredMascotCount;
        this.iconUrl = iconUrl;
    }

    public static Badge create(String code, String name, String description, Integer requiredMascotCount, String iconUrl) {
        return new Badge(code, name, description, requiredMascotCount, iconUrl);
    }
}

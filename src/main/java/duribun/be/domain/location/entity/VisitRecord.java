package duribun.be.domain.location.entity;

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

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "visit_records", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "region_id"}))
public class VisitRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "region_id", nullable = false)
    private Long regionId;

    @Column(nullable = false)
    private LocalDateTime visitedAt;

    private VisitRecord(Long userId, Long regionId, LocalDateTime visitedAt) {
        this.userId = userId;
        this.regionId = regionId;
        this.visitedAt = visitedAt;
    }

    public static VisitRecord create(Long userId, Long regionId, LocalDateTime visitedAt) {
        return new VisitRecord(userId, regionId, visitedAt);
    }
}

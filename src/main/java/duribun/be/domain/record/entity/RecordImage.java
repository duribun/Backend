package duribun.be.domain.record.entity;

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

/**
 * 여행 기록에 첨부된 사진 (최대 4장, 순서 있음).
 * 프로젝트 컨벤션에 따라 TravelRecord와 JPA 연관관계를 맺지 않고 recordId 값 참조로만 연결한다.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "record_images")
public class RecordImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_id", nullable = false)
    private Long recordId;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    private RecordImage(Long recordId, String imageUrl, Integer sortOrder) {
        this.recordId = recordId;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }

    public static RecordImage create(Long recordId, String imageUrl, Integer sortOrder) {
        return new RecordImage(recordId, imageUrl, sortOrder);
    }
}

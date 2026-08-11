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
import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "travel_records")
public class TravelRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "visited_at", nullable = false)
    private LocalDate visitedAt;

    @Column(name = "place_name", nullable = false, length = 200)
    private String placeName;

    private TravelRecord(Long userId, String title, String content, String imageUrl,
                         LocalDate visitedAt, String placeName) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.visitedAt = visitedAt;
        this.placeName = placeName;
    }

    public static TravelRecord create(Long userId, String title, String content, String imageUrl,
                                      LocalDate visitedAt, String placeName) {
        return new TravelRecord(userId, title, content, imageUrl, visitedAt, placeName);
    }

    public void update(String title, String content, String imageUrl,
                       LocalDate visitedAt, String placeName) {
        if (title != null) this.title = title;
        if (content != null) this.content = content;
        if (imageUrl != null) this.imageUrl = imageUrl;
        if (visitedAt != null) this.visitedAt = visitedAt;
        if (placeName != null) this.placeName = placeName;
    }
}

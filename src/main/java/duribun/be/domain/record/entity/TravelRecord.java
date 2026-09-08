package duribun.be.domain.record.entity;

import duribun.be.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(name = "visited_at", nullable = false)
    private LocalDate visitedAt;

    @Column(name = "place_name", nullable = false, length = 200)
    private String placeName;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Weather weather;

    @Column(nullable = false)
    private Integer temperature;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Mood mood;

    @Column(name = "favorited_at")
    private LocalDateTime favoritedAt;

    private TravelRecord(Long userId, String title, String content, LocalDate visitedAt, String placeName,
                          Double latitude, Double longitude, Weather weather, Integer temperature, Mood mood) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.visitedAt = visitedAt;
        this.placeName = placeName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.weather = weather;
        this.temperature = temperature;
        this.mood = mood;
    }

    public static TravelRecord create(Long userId, String title, String content, LocalDate visitedAt, String placeName,
                                       Double latitude, Double longitude, Weather weather, Integer temperature, Mood mood) {
        return new TravelRecord(userId, title, content, visitedAt, placeName, latitude, longitude, weather, temperature, mood);
    }

    public void update(String title, String content, LocalDate visitedAt, String placeName,
                        Double latitude, Double longitude, Weather weather, Integer temperature, Mood mood) {
        if (title != null && !title.isBlank()) this.title = title;
        if (content != null && !content.isBlank()) this.content = content;
        if (visitedAt != null) this.visitedAt = visitedAt;
        if (placeName != null && !placeName.isBlank()) this.placeName = placeName;
        if (latitude != null) this.latitude = latitude;
        if (longitude != null) this.longitude = longitude;
        if (weather != null) this.weather = weather;
        if (temperature != null) this.temperature = temperature;
        if (mood != null) this.mood = mood;
    }

    /**
     * 즐겨찾기 토글. 최초 등록 시각을 저장해두면 여러 개를 즐겨찾기했을 때
     * "누른 순서대로(오름차순: 과거→미래)" 정렬할 수 있다 (Figma 와이어프레임 주석 근거).
     */
    public void toggleFavorite(LocalDateTime now) {
        this.favoritedAt = isFavorite() ? null : now;
    }

    public boolean isFavorite() {
        return favoritedAt != null;
    }
}

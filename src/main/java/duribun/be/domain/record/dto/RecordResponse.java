package duribun.be.domain.record.dto;

import duribun.be.domain.record.entity.Mood;
import duribun.be.domain.record.entity.TravelRecord;
import duribun.be.domain.record.entity.Weather;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record RecordResponse(
        Long id,
        String title,
        String content,
        List<String> imageUrls,
        LocalDate visitedAt,
        String placeName,
        Double latitude,
        Double longitude,
        Weather weather,
        Integer temperature,
        Mood mood,
        boolean favorite,
        LocalDateTime createdAt
) {
    public static RecordResponse of(TravelRecord record, List<String> imageUrls) {
        return new RecordResponse(
                record.getId(),
                record.getTitle(),
                record.getContent(),
                imageUrls,
                record.getVisitedAt(),
                record.getPlaceName(),
                record.getLatitude(),
                record.getLongitude(),
                record.getWeather(),
                record.getTemperature(),
                record.getMood(),
                record.isFavorite(),
                record.getCreatedAt()
        );
    }
}

package duribun.be.domain.record.dto;

import duribun.be.domain.record.entity.Mood;
import duribun.be.domain.record.entity.Weather;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record UpdateRecordRequest(
        String title,
        String content,
        @Size(max = 4, message = "사진은 최대 4장까지 등록할 수 있습니다.") List<String> imageUrls,
        LocalDate visitedAt,
        String placeName,
        Double latitude,
        Double longitude,
        Weather weather,
        Integer temperature,
        Mood mood
) {
}

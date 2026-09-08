package duribun.be.domain.record.dto;

import duribun.be.domain.record.entity.Mood;
import duribun.be.domain.record.entity.Weather;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CreateRecordRequest(
        @NotBlank(message = "제목은 필수입니다.") String title,
        @NotBlank(message = "내용은 필수입니다.") String content,
        @Size(max = 4, message = "사진은 최대 4장까지 등록할 수 있습니다.") List<String> imageUrls,
        @NotNull(message = "방문 날짜는 필수입니다.") LocalDate visitedAt,
        @NotBlank(message = "장소명은 필수입니다.") String placeName,
        @NotNull(message = "위도는 필수입니다.") Double latitude,
        @NotNull(message = "경도는 필수입니다.") Double longitude,
        @NotNull(message = "날씨는 필수입니다.") Weather weather,
        @NotNull(message = "온도는 필수입니다.") Integer temperature,
        @NotNull(message = "기분은 필수입니다.") Mood mood
) {
}

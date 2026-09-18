package duribun.be.domain.record.dto;

import duribun.be.domain.record.entity.Mood;
import duribun.be.domain.record.entity.Weather;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * placeName/latitude/longitude는 장소를 등록하지 않은 기록(사진 없이 글만 쓰는 경우 등)을 허용하기 위해
 * 개별 @NotBlank/@NotNull을 두지 않는다. 대신 서비스 레이어에서 "셋 다 없거나 셋 다 있거나"만
 * 유효하도록 검증한다(RecordService#validatePlaceConsistency).
 */
public record CreateRecordRequest(
        @NotBlank(message = "제목은 필수입니다.") String title,
        @NotBlank(message = "내용은 필수입니다.") String content,
        @Size(max = 4, message = "사진은 최대 4장까지 등록할 수 있습니다.") List<String> imageUrls,
        @NotNull(message = "방문 날짜는 필수입니다.") LocalDate visitedAt,
        String placeName,
        Double latitude,
        Double longitude,
        @NotNull(message = "날씨는 필수입니다.") Weather weather,
        @NotNull(message = "온도는 필수입니다.") Integer temperature,
        @NotNull(message = "기분은 필수입니다.") Mood mood
) {
}

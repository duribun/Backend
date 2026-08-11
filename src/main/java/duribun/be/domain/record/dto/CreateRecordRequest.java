package duribun.be.domain.record.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateRecordRequest(
        @NotBlank(message = "제목은 필수입니다.") String title,
        @NotBlank(message = "내용은 필수입니다.") String content,
        String imageUrl,
        @NotNull(message = "방문 날짜는 필수입니다.") LocalDate visitedAt,
        @NotBlank(message = "장소명은 필수입니다.") String placeName
) {
}

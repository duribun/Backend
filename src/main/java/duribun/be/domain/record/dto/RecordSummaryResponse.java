package duribun.be.domain.record.dto;

import duribun.be.domain.record.entity.TravelRecord;

import java.time.LocalDate;

/**
 * content(전체 내용)를 그대로 담아 내려준다. 목록/캘린더 카드의 내용 미리보기(제목 10자,
 * 내용은 사진 유무에 따라 32자/46자로 자르고 초과 시 "..." 부착)는 FE가 truncate 유틸로
 * 처리하는 표시 로직이라 BE는 서버측에서 자르지 않고 원문을 그대로 준다.
 */
public record RecordSummaryResponse(
        Long id,
        String title,
        String content,
        String thumbnailUrl,
        LocalDate visitedAt,
        String placeName,
        boolean favorite
) {
    public static RecordSummaryResponse of(TravelRecord record, String thumbnailUrl) {
        return new RecordSummaryResponse(
                record.getId(),
                record.getTitle(),
                record.getContent(),
                thumbnailUrl,
                record.getVisitedAt(),
                record.getPlaceName(),
                record.isFavorite()
        );
    }
}

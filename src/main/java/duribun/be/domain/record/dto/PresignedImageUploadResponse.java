package duribun.be.domain.record.dto;

/**
 * S3 사진 업로드용 presigned URL 발급 응답.
 * FE는 uploadUrl로 S3에 직접 PUT 업로드한 뒤, imageUrl을 기록 생성/수정 요청에 그대로 실어 보낸다.
 */
public record PresignedImageUploadResponse(
        String uploadUrl,
        String imageUrl
) {
}

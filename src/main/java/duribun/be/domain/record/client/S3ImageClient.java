package duribun.be.domain.record.client;

import duribun.be.domain.record.config.S3Properties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Component
public class S3ImageClient {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final S3Properties s3Properties;

    public S3ImageClient(S3Presigner s3Presigner, S3Client s3Client, S3Properties s3Properties) {
        this.s3Presigner = s3Presigner;
        this.s3Client = s3Client;
        this.s3Properties = s3Properties;
    }

    public PresignedImageUpload issuePresignedUrl(Long userId, String extension) {
        String key = "records/%d/%s.%s".formatted(userId, UUID.randomUUID(), extension);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(key)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(s3Properties.presignedUrlExpireMinutes()))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);
        return new PresignedImageUpload(presigned.url().toString(), publicUrl(key));
    }

    public void deleteImage(String imageUrl) {
        String prefix = publicUrl("");
        if (!imageUrl.startsWith(prefix)) {
            return;
        }
        String key = imageUrl.substring(prefix.length());
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(key)
                .build());
    }

    private String publicUrl(String key) {
        return "https://%s.s3.%s.amazonaws.com/%s".formatted(s3Properties.bucket(), s3Properties.region(), key);
    }

    public record PresignedImageUpload(String uploadUrl, String imageUrl) {
    }
}

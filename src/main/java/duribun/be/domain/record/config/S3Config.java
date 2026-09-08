package duribun.be.domain.record.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * AWS 자격증명은 SDK 기본 자격증명 체인(환경변수 AWS_ACCESS_KEY_ID/AWS_SECRET_ACCESS_KEY,
 * 또는 배포 환경의 IAM Role)을 그대로 사용한다. 코드/설정 파일에 키를 직접 두지 않는다.
 */
@Configuration
public class S3Config {

    @Bean
    public S3Presigner s3Presigner(S3Properties s3Properties) {
        return S3Presigner.builder()
                .region(Region.of(s3Properties.region()))
                .build();
    }

    @Bean
    public S3Client s3Client(S3Properties s3Properties) {
        return S3Client.builder()
                .region(Region.of(s3Properties.region()))
                .build();
    }
}

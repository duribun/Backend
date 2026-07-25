package duribun.be.domain.user.entity;

import duribun.be.global.exception.UnsupportedProviderException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SocialProviderTest {

    @ParameterizedTest
    @CsvSource({
            "google, GOOGLE",
            "GOOGLE, GOOGLE",
            "kakao, KAKAO",
            "NAVER, NAVER",
    })
    void from_대소문자무관하게_provider_문자열을_enum으로_변환한다(String raw, SocialProvider expected) {
        assertThat(SocialProvider.from(raw)).isEqualTo(expected);
    }

    @Test
    void from_지원하지_않는_provider_문자열이면_예외를_던진다() {
        assertThatThrownBy(() -> SocialProvider.from("facebook"))
                .isInstanceOf(UnsupportedProviderException.class);
    }
}

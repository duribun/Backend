package duribun.be.domain.user.dto;

import duribun.be.domain.user.entity.Gender;
import duribun.be.domain.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record ProfileUpdateRequest(
        @NotBlank(message = "닉네임은 필수입니다.")
        @Pattern(regexp = User.NICKNAME_PATTERN, message = "닉네임은 2~10자의 한글, 영문, 숫자만 사용할 수 있습니다.")
        String nickname,
        @NotNull(message = "생년월일은 필수입니다.") LocalDate birthDate,
        @NotNull(message = "성별은 필수입니다.") Gender gender
) {
}

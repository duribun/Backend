package duribun.be.domain.user.dto;

import duribun.be.domain.user.entity.Gender;
import duribun.be.domain.user.entity.User;

import java.time.LocalDate;

public record ProfileUpdateResponse(
        String nickname,
        LocalDate birthDate,
        Gender gender
) {

    public static ProfileUpdateResponse from(User user) {
        return new ProfileUpdateResponse(user.getNickname(), user.getBirthDate(), user.getGender());
    }
}

package duribun.be.domain.user.dto;

import duribun.be.domain.user.entity.Gender;
import duribun.be.domain.user.entity.SocialProvider;
import duribun.be.domain.user.entity.User;

import java.time.LocalDate;

public record UserProfileResponse(
        String nickname,
        LocalDate birthDate,
        Gender gender,
        SocialProvider provider
) {

    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(user.getNickname(), user.getBirthDate(), user.getGender(), user.getProvider());
    }
}

package duribun.be.domain.user.service;

import duribun.be.domain.auth.dto.SocialUserInfo;
import duribun.be.domain.user.entity.Gender;
import duribun.be.domain.user.entity.SocialProvider;
import duribun.be.domain.user.entity.User;
import duribun.be.domain.user.entity.UserStatus;
import duribun.be.domain.user.repository.UserRepository;
import duribun.be.global.exception.AlreadyWithdrawnUserException;
import duribun.be.global.exception.DuplicateNicknameException;
import duribun.be.global.exception.InvalidNicknameException;
import duribun.be.global.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    private User activeUser() {
        return User.create(new SocialUserInfo("pid-1", "a@a.com", "nick"), SocialProvider.GOOGLE);
    }

    @Test
    void withdraw_정상_유저면_status가_WITHDRAWN으로_변경된다() {
        User user = activeUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.withdraw(1L);

        assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
        assertThat(user.getWithdrawnAt()).isNotNull();
    }

    @Test
    void withdraw_이미_탈퇴한_유저면_AlreadyWithdrawnUserException을_던진다() {
        User user = activeUser();
        user.withdraw();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.withdraw(1L))
                .isInstanceOf(AlreadyWithdrawnUserException.class);
    }

    @Test
    void withdraw_존재하지_않는_유저면_예외를_던진다() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.withdraw(1L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void isNicknameAvailable_형식이_올바르지_않으면_예외를_던진다() {
        assertThatThrownBy(() -> userService.isNicknameAvailable(1L, "a"))
                .isInstanceOf(InvalidNicknameException.class);

        assertThatThrownBy(() -> userService.isNicknameAvailable(1L, "닉네임!!"))
                .isInstanceOf(InvalidNicknameException.class);
    }

    @Test
    void isNicknameAvailable_다른_유저가_사용중이면_false를_반환한다() {
        when(userRepository.existsByNicknameAndIdNot("여행자", 1L)).thenReturn(true);

        assertThat(userService.isNicknameAvailable(1L, "여행자")).isFalse();
    }

    @Test
    void isNicknameAvailable_아무도_사용하지_않거나_본인이_사용중이면_true를_반환한다() {
        when(userRepository.existsByNicknameAndIdNot("여행자", 1L)).thenReturn(false);

        assertThat(userService.isNicknameAvailable(1L, "여행자")).isTrue();
    }

    @Test
    void updateProfile_정상_케이스면_닉네임_생년월일_성별이_저장된다() {
        User user = activeUser();
        when(userRepository.existsByNicknameAndIdNot("새닉네임", 1L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.updateProfile(1L, "새닉네임", LocalDate.of(2000, 1, 1), Gender.FEMALE);

        assertThat(user.getNickname()).isEqualTo("새닉네임");
        assertThat(user.getBirthDate()).isEqualTo(LocalDate.of(2000, 1, 1));
        assertThat(user.getGender()).isEqualTo(Gender.FEMALE);
    }

    @Test
    void updateProfile_다른_유저가_이미_사용중인_닉네임이면_예외를_던지고_조회하지_않는다() {
        when(userRepository.existsByNicknameAndIdNot("중복닉네임", 1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.updateProfile(1L, "중복닉네임", LocalDate.of(2000, 1, 1), Gender.MALE))
                .isInstanceOf(DuplicateNicknameException.class);

        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void updateProfile_존재하지_않는_유저면_예외를_던진다() {
        when(userRepository.existsByNicknameAndIdNot("새닉네임", 1L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile(1L, "새닉네임", LocalDate.of(2000, 1, 1), Gender.MALE))
                .isInstanceOf(UserNotFoundException.class);
    }
}

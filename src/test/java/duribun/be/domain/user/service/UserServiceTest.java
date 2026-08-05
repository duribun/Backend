package duribun.be.domain.user.service;

import duribun.be.domain.auth.dto.SocialUserInfo;
import duribun.be.domain.user.entity.SocialProvider;
import duribun.be.domain.user.entity.User;
import duribun.be.domain.user.entity.UserStatus;
import duribun.be.domain.user.repository.UserRepository;
import duribun.be.global.exception.AlreadyWithdrawnUserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
                .isInstanceOf(IllegalStateException.class);
    }
}

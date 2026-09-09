package duribun.be.domain.user.service;

import duribun.be.domain.user.entity.Gender;
import duribun.be.domain.user.entity.User;
import duribun.be.domain.user.repository.UserRepository;
import duribun.be.global.exception.AlreadyWithdrawnUserException;
import duribun.be.global.exception.DuplicateNicknameException;
import duribun.be.global.exception.InvalidNicknameException;
import duribun.be.global.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.regex.Pattern;

@Service
@Transactional
public class UserService {

    private static final Pattern NICKNAME_PATTERN = Pattern.compile(User.NICKNAME_PATTERN);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("인증된 사용자를 찾을 수 없습니다: " + userId));
        if (user.isWithdrawn()) {
            throw new AlreadyWithdrawnUserException("이미 탈퇴한 사용자입니다");
        }
        user.withdraw();
    }

    @Transactional(readOnly = true)
    public User getMyProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("인증된 사용자를 찾을 수 없습니다: " + userId));
    }

    public boolean isNicknameAvailable(Long userId, String nickname) {
        validateNicknameFormat(nickname);
        return !userRepository.existsByNicknameAndIdNot(nickname, userId);
    }

    public User updateProfile(Long userId, String nickname, LocalDate birthDate, Gender gender) {
        if (userRepository.existsByNicknameAndIdNot(nickname, userId)) {
            throw new DuplicateNicknameException("이미 사용 중인 닉네임입니다: " + nickname);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("인증된 사용자를 찾을 수 없습니다: " + userId));
        user.updateProfile(nickname, birthDate, gender);
        return user;
    }

    private void validateNicknameFormat(String nickname) {
        if (nickname == null || !NICKNAME_PATTERN.matcher(nickname).matches()) {
            throw new InvalidNicknameException("닉네임은 2~10자의 한글, 영문, 숫자만 사용할 수 있습니다");
        }
    }
}

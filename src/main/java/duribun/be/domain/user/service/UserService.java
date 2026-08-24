package duribun.be.domain.user.service;

import duribun.be.domain.user.entity.User;
import duribun.be.domain.user.repository.UserRepository;
import duribun.be.global.exception.AlreadyWithdrawnUserException;
import duribun.be.global.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

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
}

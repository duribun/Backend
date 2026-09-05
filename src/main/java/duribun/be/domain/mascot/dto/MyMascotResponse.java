package duribun.be.domain.mascot.dto;

import duribun.be.domain.mascot.entity.Mascot;
import duribun.be.domain.mascot.entity.UserMascot;

import java.time.LocalDateTime;

public record MyMascotResponse(
        Long mascotId,
        String name,
        String imageUrl,
        LocalDateTime acquiredAt
) {

    public static MyMascotResponse of(Mascot mascot, UserMascot userMascot) {
        return new MyMascotResponse(
                mascot.getId(),
                mascot.getName(),
                mascot.getImageUrl(),
                userMascot.getAcquiredAt()
        );
    }
}

package duribun.be.domain.mascot.dto;

import duribun.be.domain.mascot.entity.Mascot;
import duribun.be.domain.mascot.entity.UserMascot;

import java.time.LocalDateTime;

public record MascotResponse(
        Long mascotId,
        Long regionId,
        String name,
        String imageUrl,
        boolean acquired,
        LocalDateTime acquiredAt
) {

    public static MascotResponse of(Mascot mascot, UserMascot userMascot) {
        boolean acquired = userMascot != null;
        return new MascotResponse(
                mascot.getId(),
                mascot.getRegionId(),
                mascot.getName(),
                mascot.getImageUrl(),
                acquired,
                acquired ? userMascot.getAcquiredAt() : null
        );
    }
}

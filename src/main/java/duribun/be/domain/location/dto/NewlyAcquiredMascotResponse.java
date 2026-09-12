package duribun.be.domain.location.dto;

import duribun.be.domain.location.event.NewlyAcquiredMascotInfo;

public record NewlyAcquiredMascotResponse(
        Long mascotId,
        String name,
        String imageUrl
) {

    public static NewlyAcquiredMascotResponse from(NewlyAcquiredMascotInfo info) {
        return new NewlyAcquiredMascotResponse(info.mascotId(), info.name(), info.imageUrl());
    }
}

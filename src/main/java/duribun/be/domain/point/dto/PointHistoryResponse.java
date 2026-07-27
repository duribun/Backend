package duribun.be.domain.point.dto;

import duribun.be.domain.point.entity.PointHistory;
import duribun.be.domain.point.service.PointReason;

import java.time.LocalDateTime;

public record PointHistoryResponse(
        int amount,
        PointReason reason,
        int balanceAfter,
        LocalDateTime createdAt
) {

    public static PointHistoryResponse from(PointHistory history) {
        return new PointHistoryResponse(
                history.getAmount(), history.getReason(), history.getBalanceAfter(), history.getCreatedAt());
    }
}

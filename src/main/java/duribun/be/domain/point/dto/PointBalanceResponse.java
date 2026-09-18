package duribun.be.domain.point.dto;

public record PointBalanceResponse(int balance) {

    public static PointBalanceResponse from(int balance) {
        return new PointBalanceResponse(balance);
    }
}

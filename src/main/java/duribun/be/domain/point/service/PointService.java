package duribun.be.domain.point.service;

public interface PointService {

    void earn(Long userId, int amount, PointReason reason);

    void spend(Long userId, int amount, PointReason reason);

    int getBalance(Long userId);
}

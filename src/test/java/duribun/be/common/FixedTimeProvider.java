package duribun.be.common;

import java.time.LocalDateTime;

public class FixedTimeProvider implements TimeProvider {

    private final LocalDateTime fixedTime;

    public FixedTimeProvider(LocalDateTime fixedTime) {
        this.fixedTime = fixedTime;
    }

    @Override
    public LocalDateTime now() {
        return fixedTime;
    }
}

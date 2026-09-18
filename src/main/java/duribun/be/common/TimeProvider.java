package duribun.be.common;

import java.time.LocalDateTime;

public interface TimeProvider {
    LocalDateTime now();
}

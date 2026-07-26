package duribun.be.domain.location.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class DistanceCalculatorTest {

    @Test
    void 같은_좌표면_거리가_0이다() {
        double distance = DistanceCalculator.calculateMeters(37.7519, 128.8761, 37.7519, 128.8761);

        assertThat(distance).isEqualTo(0.0, within(0.0001));
    }

    @Test
    void 위도가_1도_차이나면_약_111_195미터다() {
        double distance = DistanceCalculator.calculateMeters(0.0, 0.0, 1.0, 0.0);

        assertThat(distance).isCloseTo(111194.93, within(1.0));
    }
}

package io.hhplus.tdd.point;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static io.hhplus.tdd.point.PointService.MAXIMUM_POINT;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PointValidatorTest {

    /**
     * 정상 범주의 포인트를 사용하는 케이스 검증
     */
    @Test
    @DisplayName("정상적인 포인트인 경우")
    void normal() {
        // given
        long givenPoint = new Random().nextLong(MAXIMUM_POINT);

        // when
        PointValidator.validate(givenPoint);

        // then
        assertThat(givenPoint)
                .isNotNull()
                .isGreaterThan(0)
                .isLessThanOrEqualTo(MAXIMUM_POINT);
    }

    /**
     * 거래하는 포인트가 null인 경우 이를 검증하는 테스트
     */
    @Test
    @DisplayName("Null인 경우")
    void caseNull() {
        Long givenPoint = null;
        assertThatThrownBy(() -> PointValidator.validate(givenPoint)).isInstanceOf(RuntimeException.class);
    }

    /**
     * 거래하는 포인트가 음수인 경우 이를 검증하는 테스트
     */
    @Test
    @DisplayName("음수인 경우")
    void caseNegative() {
        Long givenPoint = -1L;
        assertThatThrownBy(() -> PointValidator.validate(givenPoint)).isInstanceOf(RuntimeException.class);
    }

    /**
     * 최대 충전잔고를 초과하는 포인트를 거래하려고하는 경우 검증
     */
    @Test
    @DisplayName("최대거래 포인트")
    void overMaximum() {
        Long givenPoint = new Random().nextLong(MAXIMUM_POINT + 1L, Long.MAX_VALUE);
        assertThatThrownBy(() -> PointValidator.validate(givenPoint)).isInstanceOf(RuntimeException.class);
    }
}
package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PointValidatorTest {

    @Test
    @DisplayName("Null인 경우")
    void caseNull() {
        Long givenPoint = null;
        assertThatThrownBy(() -> PointValidator.validate(givenPoint)).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("음수인 경우")
    void caseNegative() {
        Long givenPoint = -1L;
        assertThatThrownBy(() -> PointValidator.validate(givenPoint)).isInstanceOf(RuntimeException.class);
    }
}
package io.hhplus.tdd.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

class UserValidatorTest {

    @Test
    @DisplayName("사용자 ID 검증")
    void validation() {
        long givenId = new Random().nextLong(10000) * -1;
        Assertions.assertThatThrownBy(() -> UserValidator.validate(givenId)).isIn(RuntimeException.class);
    }
}
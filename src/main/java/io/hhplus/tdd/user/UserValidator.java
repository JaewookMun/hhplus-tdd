package io.hhplus.tdd.user;

public class UserValidator {
    public static void validate(Long userId) {
        if (userId == null) throw new RuntimeException("userId can not be null.");
        if (userId <= 0) throw new RuntimeException(String.format("userId can not be zero or negative. - given userId: %d", userId));
    }
}

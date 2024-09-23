package io.hhplus.tdd.point;

public class PointValidator {
    public static void validate(Long point) {
        if (point == null) throw new RuntimeException("point can not be null.");
        if (point < 0) throw new RuntimeException("point can not be negative.");
    }
}
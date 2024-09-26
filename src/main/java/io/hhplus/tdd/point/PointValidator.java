package io.hhplus.tdd.point;

import static io.hhplus.tdd.point.PointService.MAXIMUM_POINT;

public class PointValidator {

    /**
     * 포인트는 null 혹은 음수가 될 수 없으며 100만 이하의 포인트만 거래(충전/사용) 가능합니다.
     * @param point
     */
    public static void validate(Long point) {
        if (point == null) throw new RuntimeException("point can not be null.");
        if (point < 0) throw new RuntimeException("point can not be negative.");
        if (point > MAXIMUM_POINT) throw new RuntimeException(String.format("you can just dael below or equal %d point at a once", MAXIMUM_POINT));
    }
}
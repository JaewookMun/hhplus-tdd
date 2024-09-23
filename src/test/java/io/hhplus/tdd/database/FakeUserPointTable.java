package io.hhplus.tdd.database;

import io.hhplus.tdd.point.UserPoint;

import java.util.HashMap;
import java.util.Map;

public class FakeUserPointTable extends UserPointTable {
    private final Map<Long, UserPoint> userPoints = new HashMap<>();

    @Override
    public UserPoint selectById(Long id) {
        return userPoints.getOrDefault(id, UserPoint.empty(id));
    }

    @Override
    public UserPoint insertOrUpdate(long id, long amount) {
        UserPoint userPoint = new UserPoint(id, amount, System.currentTimeMillis());
        userPoints.put(id, userPoint);
        return userPoint;
    }
}

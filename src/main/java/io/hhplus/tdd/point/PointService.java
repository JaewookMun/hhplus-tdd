package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public UserPoint getUserPoint(long id) {

        return userPointTable.selectById(id);
    }

    public List<PointHistory> getPointHistory(long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }

    public UserPoint chargePoint(long id, long amount) {
        UserPoint userPoint = userPointTable.selectById(id);
        long totalPoint = userPoint.point() + amount;

        return userPointTable.insertOrUpdate(id, totalPoint);
    }

    public UserPoint reducePoint(long id, long amount) {
        UserPoint userPoint = userPointTable.selectById(id);
        long left = userPoint.point() - amount;

        return userPointTable.insertOrUpdate(id, left);
    }
}

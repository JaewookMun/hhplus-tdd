package io.hhplus.tdd.database;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;

import java.util.ArrayList;
import java.util.List;

public class FakePointHistoryTable extends PointHistoryTable {

    private final List<PointHistory> pointHistories= new ArrayList<PointHistory>();
    private long cursor = 1;

    @Override
    public PointHistory insert(long userId, long amount, TransactionType type, long updateMillis) {
        PointHistory pointHistory = new PointHistory(cursor++, userId, amount, type, updateMillis);
        pointHistories.add(pointHistory);
        return pointHistory;
    }

    @Override
    public List<PointHistory> selectAllByUserId(long userId) {
        return pointHistories.stream().filter(pointHistory -> pointHistory.userId() == userId).toList();
    }
}

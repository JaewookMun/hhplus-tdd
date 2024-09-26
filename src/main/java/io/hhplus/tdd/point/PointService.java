package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.MinusPointException;
import io.hhplus.tdd.exception.OutOfMaximumPointException;
import io.hhplus.tdd.user.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static io.hhplus.tdd.point.TransactionType.CHARGE;
import static io.hhplus.tdd.point.TransactionType.USE;
import static io.hhplus.tdd.user.UserValidator.validate;
import static java.lang.System.currentTimeMillis;

@Service
@RequiredArgsConstructor
public class PointService {

    /** 최대 포인트 잔고 - 100만 포인트 */
    protected static final long MAXIMUM_POINT = 1000000;
    private final Map<Long, Lock> userLocks = new ConcurrentHashMap<>();

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public UserPoint getUserPoint(long id) {
        validate(id);
        return userPointTable.selectById(id);
    }

    public List<PointHistory> getPointHistory(long id) {
        validate(id);
        return pointHistoryTable.selectAllByUserId(id);
    }

    public UserPoint chargePoint(long id, long amount) {
        UserValidator.validate(id);
        PointValidator.validate(amount);

        Lock lock = userLocks.computeIfAbsent(id, k -> new ReentrantLock());
        lock.lock();

        try {
            long totalPoint = userPointTable.selectById(id).point() + amount;
            if (totalPoint > MAXIMUM_POINT)
                throw new OutOfMaximumPointException(String.format("최대로 충전할 수 있는 포인트는 %d point 입니다.", MAXIMUM_POINT));

            UserPoint userPoint = userPointTable.insertOrUpdate(id, totalPoint);
            pointHistoryTable.insert(id, amount, CHARGE, currentTimeMillis());

            return userPoint;
        } finally {
            lock.unlock();
        }
    }

    public UserPoint reducePoint(long id, long amount) {
        UserValidator.validate(id);
        PointValidator.validate(amount);

        Lock lock = userLocks.computeIfAbsent(id, k -> new ReentrantLock());
        lock.lock();

        try {
            long balance = userPointTable.selectById(id).point() - amount;
            if (balance < 0) throw new MinusPointException("포인트 잔고가 부족합니다.");

            UserPoint userPoint = userPointTable.insertOrUpdate(id, balance);
            pointHistoryTable.insert(id, amount, USE, currentTimeMillis());

            return userPoint;
        } finally {
            lock.unlock();
        }
    }
}

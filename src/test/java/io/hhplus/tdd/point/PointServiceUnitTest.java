package io.hhplus.tdd.point;

import io.hhplus.tdd.database.FakePointHistoryTable;
import io.hhplus.tdd.database.FakeUserPointTable;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.hhplus.tdd.point.TransactionType.CHARGE;
import static java.lang.System.currentTimeMillis;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 테스트 케이스의 작성 및 작성 이유를 주석으로 작성
 *
 * PointService class Unit Test
 */
class PointServiceUnitTest {

    private PointService pointService;

    private PointHistoryTable fakePointHistoryTable = new FakePointHistoryTable();
    private UserPointTable fakeUserPointTable = new FakeUserPointTable();

    /**
     * 단위 테스트 진행을 위한 환경 초기화
     */
    @BeforeEach
    void setUp() {
        pointService = new PointService(fakeUserPointTable, fakePointHistoryTable);
    }

    /**
     * 성공 케이스 - '특정 유저의 포인트를 조회하는 기능'이 정상적으로 동작하는 케이스
     */
    @Test
    @DisplayName("성공 케이스 - 특정 유저 포인트 조회")
    void successCaseForGetUserPoint() {
        // given
        fakeUserPointTable.insertOrUpdate(1L, 1000);

        // when
        UserPoint foundUserPoint = pointService.getUserPoint(1L);

        // then
        assertThat(foundUserPoint.point()).isEqualTo(1000);
    }

    /**
     * 성공 케이스 - '특정 유저의 포인트 충전/이용 내역을 조회하는 기능'이 정상적으로 동작하는 케이스
     */
    @Test
    @DisplayName("성공 케이스 - 특정 유저의 포인트 충전/이용 내역 조회")
    void successCaseForGetPointHistory() {
        // given
        fakePointHistoryTable.insert(1L, 1500, CHARGE, currentTimeMillis());
        fakePointHistoryTable.insert(1L, 2000, CHARGE, currentTimeMillis());
        fakePointHistoryTable.insert(1L, 4000, CHARGE, currentTimeMillis());

        // when
        List<PointHistory> pointHistory = pointService.getPointHistory(1L);

        // then
        assertThat(pointHistory.size()).isEqualTo(3);
    }

    /**
     * 성공 케이스 - '특정 유저의 포인트를 충전하는 기능'이 정삭적으로 동작하는 케이스
     */
    @Test
    @DisplayName("성공 케이스 - 특정 유저의 포인트를 충전하는 기능")
    void successCaseForChargePoint() {
        // given
        fakeUserPointTable.insertOrUpdate(1L, 1000);

        // when
        UserPoint userPoint = pointService.chargePoint(1L, 2000);

        // then
        assertThat(userPoint.point()).isEqualTo(3000);
    }

    /**
     * 성공 케이스 - '특정 유저의 포인트를 사용하는 기능'이 정삭적으로 동작하는 케이스
     */
    @Test
    @DisplayName("성공 케이스 - 특정 유저의 포인트를 사용하는 기능")
    void successChargePoint() {
        // given
        fakeUserPointTable.insertOrUpdate(1L, 2000);

        // when
        UserPoint userPoint = pointService.reducePoint(1L, 500);

        // then
        assertThat(userPoint.point()).isEqualTo(1500);
    }
}
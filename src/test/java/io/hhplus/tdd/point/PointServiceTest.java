package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.CompletableFuture.runAsync;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.MethodMode.BEFORE_METHOD;

/**
 * PointService class Integration Test
 */
@SpringBootTest
class PointServiceTest {

    @Autowired
    private PointService pointService;

    /**
     * 동시에 여러 요청이 들어오더라도 하나의 요청씩 제어되는지 확인하는 테스트
     */
    @Test
    @DisplayName("단일 사용자 대상")
    @DirtiesContext(methodMode = BEFORE_METHOD) // 테스트의 격리를 위해 사용
    void soloUser() throws ExecutionException, InterruptedException {
        //given
        pointService.chargePoint(1L, 1000);

        CompletableFuture<Void> allTask = CompletableFuture.allOf(
                runAsync(() -> pointService.chargePoint(1L, 700)),
                runAsync(() -> pointService.chargePoint(1L, 1000)),
                runAsync(() -> pointService.chargePoint(1L, 400)),
                runAsync(() -> pointService.chargePoint(1L, 500)),
                runAsync(() -> pointService.reducePoint(1L, 500)),
                runAsync(() -> pointService.reducePoint(1L, 400))
        );

        //when
        allTask.join();

        //then
        UserPoint userPoint = pointService.getUserPoint(1L);
        assertThat(userPoint.point()).isEqualTo(1000 + 1700);

        // 참고용 로그
        pointService.getPointHistory(1l).stream().filter(h -> h.id() > 1)
                .sorted((a, b) -> (int)(a.id() - b.id()))
                .forEach(System.out::println);
        System.out.println("\nuserPoint = " + userPoint);
    }

    /**
     * 동시에 여러 요청(무작위 순서)이 들어오더라도 사용자별로 요청 제어되는지 확인하는 테스트
     */
    @Test
    @DisplayName("다중 사용자 대상 - shuffle")
    @DirtiesContext(methodMode = BEFORE_METHOD) // 테스트의 격리를 위해 사용
    void multipleUser() throws ExecutionException, InterruptedException {
        //given
        pointService.chargePoint(1L, 1000);
        pointService.chargePoint(2L, 1000);

        List<Runnable> tasks = new ArrayList<>();
        tasks.addAll(Arrays.asList(
            // user1
            () -> pointService.chargePoint(1L, 700),
            () -> pointService.chargePoint(1L, 400),
            () -> pointService.chargePoint(1L, 500),
            () -> pointService.reducePoint(1L, 500),
            () -> pointService.reducePoint(1L, 400),
            // user2
            () -> pointService.chargePoint(2L, 300),
            () -> pointService.chargePoint(2L, 400),
            () -> pointService.chargePoint(2L, 500),
            () -> pointService.reducePoint(2L, 300),
            () -> pointService.reducePoint(2L, 400)
        ));
        
        // 무작위 순서로 변경
        Collections.shuffle(tasks);

        CompletableFuture<Void> allTask =
                CompletableFuture.allOf(
                        tasks.stream().map(task -> runAsync(task)).toArray(CompletableFuture[]::new)
                );

        //when
        allTask.thenRun(() -> System.out.println("[FINISH] Thread work"));
        allTask.join();

        //then
        UserPoint userPoint1 = pointService.getUserPoint(1L);
        UserPoint userPoint2 = pointService.getUserPoint(2L);
        assertThat(userPoint1.point()).isEqualTo(1000 + 700);
        assertThat(userPoint2.point()).isEqualTo(1000 + 500);

        // 참고용 로그
        pointService.getPointHistory(1l).forEach(System.out::println);
        pointService.getPointHistory(2l).forEach(System.out::println);
    }

    /**
     * 모든 요청을 동기화하는 것이 아니라 사용자별로 동기화 처리가 정상적으로 되는지 확인하는 테스트
     * 예) 사용자1이 요청하면 이후 사용자1의 정보로 오는 요청은 동기화 처리되어 Lock이 걸리지만
     *     다른 사용자 (사용자 2, 사용자3 ...)가 보낸 요청은 Lock 처리가 되지 않는다.
     */
    @Test
    @DisplayName("다중 사용자 - 사용자 간 작업분리")
    @DirtiesContext(methodMode = BEFORE_METHOD) // 테스트의 격리를 위해 사용
    void isolatedUserTest() {
        //given
        int numberOfUsers = 5;
        int operationCountPerUser = 10;
        long chargeAmount = new Random().nextLong(100, 301);
        Map<Long, List<Long>> operationTimesPerUser = new ConcurrentHashMap<>();
        List<CompletableFuture<Void>> allTasks = new ArrayList<>();

        // when
        initializeTasksWith(numberOfUsers, operationCountPerUser, chargeAmount, operationTimesPerUser, allTasks);
        CompletableFuture.allOf(allTasks.toArray(new CompletableFuture[0])).join();

        // then
        for (long userId=1; userId<=numberOfUsers; userId++) {
            List<Long> operationTimes = operationTimesPerUser.get(userId);

            // 참고용 로그 - 아래 로그를 확인하면 각 사용자별 작업시간은 순차적으로 증가함을 확인할 수 있다.
            System.out.printf("user-%d (ms): ", userId);
            operationTimes.forEach(t -> System.out.print(t/1000000 + " "));
            System.out.println();

            if (userId > 1) {
                List<Long> previousOperationTimes = operationTimesPerUser.get(userId - 1);
                // 각 사용자별 첫 작업은 다른 사용자의 마지막 작업보다 소요시간이 더 작음을 검증
                assertThat(operationTimes.get(0)).isLessThan(previousOperationTimes.get(previousOperationTimes.size() - 1));
            }
        }
    }

    private void initializeTasksWith(
            int numberOfUsers,
            int operationCountPerUser,
            long chargeAmount,
            Map<Long, List<Long>> operationTimesPerUser,
            List<CompletableFuture<Void>> allTasks) {

        for (long userId = 1; userId<= numberOfUsers; userId++) {
            operationTimesPerUser.put(userId, Collections.synchronizedList(new ArrayList<>()));

            long currentUserId = userId;
            for (int i = 0; i < operationCountPerUser; i++) {
                CompletableFuture<Void> task = runAsync(() -> {
                    long start = System.nanoTime();
                    pointService.chargePoint(currentUserId, chargeAmount);
                    long end = System.nanoTime();

                    operationTimesPerUser.get(currentUserId).add(end - start);
                });
                allTasks.add(task);
            }
        }
    }

    /**
     * 회원가입되어있는 모든 사용자를 대상으로 포인트 조회가 가능하다.
     */
    @Test
    @DisplayName("임의의 사용자에 대한 포인트 조회")
    void findPointForAnyone() {
        // given
        long chosenUserId = new Random().nextLong(Long.MAX_VALUE - 1) + 1L;

        // when
        UserPoint userPoint = pointService.getUserPoint(chosenUserId);

        // then
        assertThat(userPoint.point()).isGreaterThanOrEqualTo(0L);
    }

    /**
     * 회원가입되어있는 모든 사용자를 대상으로 포인트 충전/사용 이력 조회가 가능하다.
     */
    @Test
    @DisplayName("임의의 사용자에 대한 포인트 이력 조회")
    void findPointHistoryForAnyone() {
        // given
        long chosenUserId = new Random().nextLong(Long.MAX_VALUE - 1) + 1L;

        // when
        List<PointHistory> pointHistory = pointService.getPointHistory(chosenUserId);

        // then
        assertThat(pointHistory.size()).isGreaterThanOrEqualTo(0);
    }
}
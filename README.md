## 동시성제어에 대한 분석 및 보고서 <br> [ concurrency control report ]

---

<br>

### 동시성 제어란?
동시에 수행되는 동작들의 정확한 결과가 발생하는 것을 보증함을 의미한다. <br>
이를 위해서는 동시에 접근하는 데이터의 일관성을 보장하고 충돌을 방지해야한다.

``` 예시
(스프링을 예시로 생각해보자)
일반적으로 등록된 스프링 빈 인스턴스는 한개만 생성되어 모든 HTTP 요청이 공유하는 자원이다. 

인스턴스 필드를 활용한 비즈니스 로직을 가지고 있는 Sample.request() 메서드를 가정하자.
해당 메서드를 호출하는 요청이 2개(A, B) 들어왔을 때,
A가 처리되고 있는 상황에서 B에 의해 Sample의 인스턴스 필드가 변경된다면 
A요청은 정확한 결과값을 받지 못한다.

동시성 제어는 이러한 실패없이 요청에 대한 정확한 결과를 보장하기 위해 필요하다. 
```

<br><br><br>

### synchronized vs Lock
동시성 제어를 위해 다양한 방법이 존재하지만 현재 프로젝트에서는 동기처리 기능을 활용한다. 이때, 
`synchronized` 키워드와 `Lock` 인터페이스 모두 동시성 제어(동기화 처리)를 위해 사용할 수 있다. <br>
둘 모두 쓰레드의 동기화 기능을 제공하지만, 단순히 블록({})을 통해 자동으로 락을 관리하는 `synchronized`와 달리 <br>
`Lock` 인터페이스는 여러가지 다양한 기능을 통해 개발자에게 편의를 제공한다.
<br>
특히 `Lock` 인터페이스의 구현체(대표적으로 `ReentrantLock`)는 공정성 모드를 지원하여 먼저 락을 요청한 스레드가 락을 얻는
공정성을 제어할 수 있지만, `synchronized`는 이러한 기능을 제공하지 않는다.

<br>

``` java
// sysnchronized example-1: 메서드 동기화
public synchronized void invoke() {
    // access to the shared resource
}

// sysnchronized example-2: 동기화 블록
public void invoke() {
    synchronized(this) {
        // access to the shared resource
    }
}

// Lock interface example
Lock lock = new ReentrantLock();
lock.lock();
try {
    // access to the shared resource
} finally {
    lock.unlock();
}

```

<br><br><br>


### 성능 이슈
본 프로젝트는 사용자의 포인트에 대한 CRUD 기능을 다룬다.
포인트 충전 및 사용 기능은 동시성 제어가 필요하고 여기서는 동기처리를 통해 다루었다. <br>
멀티쓰레드 환경에서 동기처리된 비즈니스 로직이 수행되는 동안 같이 진입한 다른 쓰레드들은
대기를 하게되며(교착상태 - deadlock) 이는 성능 이슈와 연결된다. <br>

아래 이미지는 PointService의 통합테스트인 PointServiceTest.isolatedUserTest()의 수행 중
생성된 로그다. 5명의 사용자 Id를 가지고 각각 10번의 충전 요청을 비동기적으로 보내는 테스트를 수행했다. <br>

포인트 처리 로직을 사용자에 따라 동기화 하였기 때문에 사용자에 따라 처리 속도는 영향을 받지 않지만,
동일 사용자에게 요청이 올수록 처리속도는 증가함을 확인할 수 있다.<br>
(user-1의 경우 436 milliseconds 에서 나중에는 2616 milliseconds가 소요됨)


![image](https://github.com/user-attachments/assets/033c0a36-1af6-4655-93be-2d1f9918b0c9)

<br><br><br>


### 참고

- 향후 학습 키워드 <br>
  : 자료구조(Future, Flow, Queue, Atomic, ConcurrentHashMap, ... etc) <br>
  : MessageQueue(Kafka, RabbitMQ, ... etc) <br><br>

- Guide to java.util.concurrent.Locks (baeldung blog) : <br>
  https://www.baeldung.com/java-concurrent-locks
- Reading and Writing With a ConcurrentHashMap (baeldung blog) : <br>
  https://www.baeldung.com/concurrenthashmap-reading-and-writing

<br><br><br>
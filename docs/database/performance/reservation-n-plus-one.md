# Reservation List N+1 Performance Analysis

> GarageCare Database Performance Experiment  
> Status: Completed  
> Last Updated: 2026-08-28

---

## 1. Overview

GarageCare의 예약 목록 조회 과정에서
`Reservation`과 `Vehicle` 사이에 발생하는 N+1 문제를 재현하고 개선한다.

단순히 Fetch 전략을 변경하는 것이 아니라 다음 과정으로 개선 효과를 검증한다.

```text
문제 재현
→ 측정
→ 원인 분석
→ 개선
→ 동일 조건 재측정
→ 회귀 테스트
```

이번 실험 이후에는 예약 목록 Query 자체의 효율성을 확인하기 위해
Index 및 Execution Plan 분석을 진행한다.

---

## 2. Test Environment

### Application

- Java 17
- Spring Boot 4.1.0
- Spring Data JPA
- Hibernate ORM 7.4.1.Final
- JUnit 5

### Database

- H2 2.4.240
- In-Memory
- Transaction Isolation: READ_COMMITTED

### Test Data

```text
Member      : 1
Vehicle     : 10
Reservation : 10
```

각 Reservation은 서로 다른 Vehicle을 참조하도록 구성하였다.

```text
Reservation 1  → Vehicle 1
Reservation 2  → Vehicle 2
...
Reservation 10 → Vehicle 10
```

동일한 Vehicle을 공유할 경우 Persistence Context의 1차 캐시로 인해
N+1 문제가 명확하게 재현되지 않을 수 있기 때문이다.

---

## 3. Problem

예약 목록에서는 예약 정보와 함께 차량 정보가 필요하다.

`Reservation → Vehicle` 연관관계는 LAZY Loading을 사용하므로
Reservation 목록만 먼저 조회한 뒤 다음과 같이 Vehicle에 접근하면
추가 SELECT가 발생할 수 있다.

```java
reservation.getVehicle()
        .getVehicleNumber();
```

예상되는 Query 구조는 다음과 같다.

```text
Reservation 목록 조회 : 1
Vehicle 개별 조회     : N

Total                 : 1 + N
```

Reservation 10건을 조회하면 총 11개의 Statement가 발생할 것으로 예상하였다.

---

## 4. Measurement

Hibernate Statistics의 PreparedStatement Count를 이용하여 측정하였다.

```properties
spring.jpa.properties.hibernate.generate_statistics=true
```

테스트 데이터 생성 과정의 영향을 제거하기 위해 측정 전에
Persistence Context와 Statistics를 초기화하였다.

```java
entityManager.flush();
entityManager.clear();

statistics.clear();
```

이후 예약 목록을 조회하고 실제 사용 상황처럼 Vehicle 정보에 접근하였다.

```java
List<Reservation> reservations =
        reservationRepository
                .findAllByMemberIdOrderByReservationDateDescReservationTimeDesc(
                        member.getId()
                );

reservations.forEach(
        reservation ->
                reservation.getVehicle()
                        .getVehicleNumber()
);
```

Statement 수는 다음과 같이 측정하였다.

```java
long queryCount =
        statistics.getPrepareStatementCount();
```

> 문서에서는 편의상 `Query Count`라고 표현하지만,
> 실제 측정값은 JDBC PreparedStatement 준비 횟수이다.

---

## 5. Before

Fetch 전략을 별도로 적용하지 않은 상태에서 측정하였다.

```java
List<Reservation>
findAllByMemberIdOrderByReservationDateDescReservationTimeDesc(
        Long memberId
);
```

결과:

```text
===== Query Count: 11 =====
```

실행 구조는 다음과 같았다.

```text
Reservation 목록 조회
        ↓
1 Query

Vehicle 1  → +1
Vehicle 2  → +1
...
Vehicle 10 → +1

Total
1 + 10 = 11
```

| 항목 | Before |
|---|---:|
| Reservation | 10 |
| Vehicle | 10 |
| Reservation Query | 1 |
| 추가 Vehicle Query | 10 |
| Query Count | 11 |
| N+1 | 발생 |

예상했던 `1 + N` 형태의 N+1 문제가 실제로 재현되었다.

---

## 6. Cause Analysis

문제의 원인은 LAZY Loading 자체가 아니라,
**Vehicle 정보가 필요한 예약 목록에서도 Reservation만 먼저 조회했다는 것**이다.

```text
Reservation 목록 조회
        ↓
Vehicle은 LAZY 상태
        ↓
Vehicle 정보 접근
        ↓
추가 SELECT
        ↓
Reservation마다 반복
        ↓
N+1
```

따라서 모든 연관관계를 EAGER로 변경하지 않고,
Vehicle이 필요한 예약 목록 Query에서만 함께 조회하기로 하였다.

---

## 7. Optimization

예약 목록 Repository 메서드에 `@EntityGraph`를 적용하였다.

```java
@EntityGraph(attributePaths = "vehicle")
List<Reservation>
findAllByMemberIdOrderByReservationDateDescReservationTimeDesc(
        Long memberId
);
```

기본 연관관계는 LAZY로 유지한다.

```text
Default

Reservation → Vehicle
LAZY
```

Vehicle이 필요한 예약 목록에서만 명시적으로 함께 조회한다.

```text
Reservation List

Reservation + Vehicle
        ↑
   @EntityGraph
```

이를 통해 Vehicle이 필요하지 않은 다른 조회까지
불필요하게 EAGER Loading되는 것을 방지한다.

---

## 8. After

`@EntityGraph` 적용 후 동일한 조건으로 다시 측정하였다.

결과:

```text
===== Query Count: 1 =====
```

Reservation과 필요한 Vehicle을 함께 조회하면서
Reservation별 추가 Vehicle SELECT가 제거되었다.

### Before / After

| 항목 | Before | After |
|---|---:|---:|
| Reservation | 10 | 10 |
| Vehicle | 10 | 10 |
| Query Count | 11 | 1 |
| 추가 Vehicle Query | 10 | 0 |
| N+1 | 발생 | 제거 |
| Fetch Strategy | LAZY 개별 접근 | `@EntityGraph` |

결과적으로:

```text
Before
11 Statements

    ↓ @EntityGraph

After
1 Statement
```

동일한 테스트 조건에서 JDBC PreparedStatement 측정 횟수가
**11회에서 1회로 감소하였다.**

---

## 9. Regression Test

향후 Repository 변경으로 N+1 문제가 다시 발생하지 않도록
Query Count를 테스트에서 검증한다.

```java
long queryCount =
        statistics.getPrepareStatementCount();

assertThat(queryCount)
        .isEqualTo(1);
```

이를 통해 Fetch 전략 변경으로 추가 Statement가 발생하면
테스트 단계에서 감지할 수 있도록 하였다.

```text
문제 재현
→ 개선
→ 수치 검증
→ 회귀 테스트
```

---

## 10. Result

이번 실험에서 확인한 결과는 다음과 같다.

```text
N+1 재현
        ↓
11 Statements
        ↓
원인 분석
        ↓
@EntityGraph 적용
        ↓
1 Statement
        ↓
Regression Test
```

핵심적으로 확인한 내용은 다음과 같다.

- LAZY Loading 자체가 N+1의 문제는 아니다.
- Use Case에 필요한 연관관계를 조회 시점에 적절히 Fetch해야 한다.
- Persistence Context가 성능 테스트 결과에 영향을 줄 수 있으므로 `flush()`와 `clear()`가 필요하다.
- 성능 문제는 추측하지 않고 실제 Statement 수로 확인해야 한다.
- 개선 결과를 회귀 테스트로 유지할 수 있다.

이번 실험에서 약 `90.9%` 감소한 것은 서비스 응답 시간이 아니라
**JDBC PreparedStatement 측정 횟수**이다.

따라서 결과는 다음과 같이 해석한다.

```text
동일한 테스트 조건에서
JDBC PreparedStatement 측정 횟수가
11회에서 1회로 감소하였다.
```

---

## 11. Limitations

이번 실험에는 다음 한계가 있다.

- H2 In-Memory Database 사용
- Reservation 10건 기준
- 실제 운영 트래픽 없음
- 실제 네트워크 통신 없음
- Query 실행 시간 미측정
- PostgreSQL 실행계획 미확인

따라서 이번 결과만으로 실제 서비스 응답 시간이나
처리량이 동일한 비율로 개선되었다고 판단하지 않는다.

---

## 12. Next Experiment

N+1 개선을 통해 DB에 전달되는 Statement 수를 줄였다.

하지만 Query 수가 1개로 감소했다고 해서
남은 Query 자체가 효율적으로 실행된다는 의미는 아니다.

따라서 다음 단계에서는 회원별 예약 목록 Query의
**Index와 Execution Plan**을 분석한다.

```text
N+1 Analysis

11 Statements
      ↓
1 Statement

      ↓

Index / Execution Plan Analysis

남은 1개의 Query는
DB 내부에서 어떻게 실행되는가?
```

대상 Query:

```sql
SELECT *
FROM reservations
WHERE member_id = ?
ORDER BY reservation_date DESC,
         reservation_time DESC;
```

관련 문서:

```text
docs/database/performance/reservation-index.md
```

---

## 13. Related

### Parent

```text
docs/database/database-performance.md
```

### Next Experiment

```text
docs/database/performance/reservation-index.md
```

### Related Source

```text
src/main/java/com/hyunu/garagecare/reservation/
src/test/java/com/hyunu/garagecare/reservation/repository/
```
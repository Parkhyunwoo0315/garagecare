# GarageCare Database Performance

> Version: 1.0.0  
> Status: In Progress  
> Last Updated: 2026-08-27

---

## 1. Overview

GarageCare에서 발생할 수 있는 데이터베이스 성능 문제를 직접 재현하고,
개선 전후의 결과를 측정하여 기록한다.

단순히 JPA 최적화 기능을 적용하는 것에 그치지 않고 다음 과정을 통해
문제의 원인과 개선 효과를 검증하는 것을 목표로 한다.

```text
문제 재현
→ 측정
→ 원인 분석
→ 개선
→ 동일 조건 재측정
→ 회귀 테스트
```

현재 첫 번째 성능 개선 대상으로
예약 목록 조회 과정에서 발생하는 N+1 문제를 분석하였다.

---

## 2. Test Environment

### Application

- Java: 17
- Spring Boot: 4.1.0
- Spring Data JPA
- Hibernate ORM: 7.4.1.Final

### Database

- Database: H2
- Version: 2.4.240
- Environment: In-Memory
- Transaction Isolation Level: READ_COMMITTED

### Test Data

- Member: 1
- Vehicle: 10
- Reservation: 10
- 각 Reservation은 서로 다른 Vehicle을 참조

---

## 3. Measurement Method

Hibernate Statistics를 사용하여
조회 과정에서 준비된 JDBC Statement 수를 측정하였다.

테스트 환경에서는 다음 설정을 활성화하였다.

```properties
spring.jpa.properties.hibernate.generate_statistics=true
```

Hibernate의 `Statistics` 객체는 다음과 같이 가져온다.

```java
SessionFactory sessionFactory =
        entityManagerFactory.unwrap(SessionFactory.class);

Statistics statistics =
        sessionFactory.getStatistics();
```

측정 전에는 테스트 데이터 생성 과정에서 발생한 INSERT Statement가
결과에 포함되지 않도록 다음 순서로 테스트를 진행하였다.

```java
entityManager.flush();
entityManager.clear();

statistics.clear();
```

이후 조회 로직을 실행하고 다음 값을 확인하였다.

```java
long queryCount =
        statistics.getPrepareStatementCount();
```

> 이 문서에서 `Query Count`는 편의상 사용하는 표현이며,
> 실제 측정값은 Hibernate Statistics의
> `getPrepareStatementCount()`를 통해 확인한 JDBC PreparedStatement 준비 횟수이다.

---

## 4. Reservation List N+1

### 4.1 Purpose

로그인 회원의 예약 목록을 조회할 때
`Reservation`과 `Vehicle` 사이에서 N+1 문제가 발생하는지 확인한다.

예약 목록에서는 각 예약에 대해 다음과 같은 정보가 필요하다.

- 예약 날짜
- 예약 시간
- 예약 상태
- 차량 정보

`Reservation` 목록을 조회한 후
각 Reservation의 Vehicle 정보에 접근하는 과정에서
추가 SELECT가 발생할 가능성이 있으므로 이를 직접 측정하였다.

---

### 4.2 Entity Access Structure

테스트 데이터는 다음과 같이 구성하였다.

```text
Member
  │
  ├── Vehicle 1
  │      └── Reservation 1
  │
  ├── Vehicle 2
  │      └── Reservation 2
  │
  ├── Vehicle 3
  │      └── Reservation 3
  │
  ...
  │
  └── Vehicle 10
         └── Reservation 10
```

하나의 Member에 Vehicle 10대와 Reservation 10건을 생성하고,
각 Reservation이 서로 다른 Vehicle을 참조하도록 구성하였다.

동일한 Vehicle을 여러 Reservation에서 공유하면
Persistence Context의 1차 캐시로 인해 이미 조회된 Vehicle이 재사용될 수 있다.

따라서 N+1 문제를 명확하게 재현하기 위해
각 Reservation에 서로 다른 Vehicle을 할당하였다.

---

### 4.3 Test Scenario

#### Given

다음 테스트 데이터를 생성한다.

```text
Member      : 1
Vehicle     : 10
Reservation : 10
```

데이터 생성 후 Persistence Context의 영향을 제거하기 위해
`flush()`와 `clear()`를 수행한다.

```java
entityManager.flush();
entityManager.clear();
```

이후 Hibernate Statistics를 초기화한다.

```java
statistics.clear();
```

#### When

회원 ID를 기준으로 예약 목록을 조회한다.

```java
List<Reservation> reservations =
        reservationRepository
                .findAllByMemberIdOrderByReservationDateDescReservationTimeDesc(
                        member.getId()
                );
```

이후 실제 예약 목록에서 차량 정보를 사용하는 상황을 재현하기 위해
각 Reservation의 Vehicle에 접근한다.

```java
reservations.forEach(
        reservation ->
                reservation.getVehicle()
                        .getVehicleNumber()
);
```

#### Then

조회 과정에서 준비된 JDBC Statement 수를 확인한다.

```java
long queryCount =
        statistics.getPrepareStatementCount();
```

---

## 5. Before Optimization

최적화 전 Repository에서는
Reservation과 Vehicle을 함께 조회하기 위한 별도의 Fetch 전략을 적용하지 않았다.

```java
List<Reservation>
findAllByMemberIdOrderByReservationDateDescReservationTimeDesc(
        Long memberId
);
```

동일한 조건에서 테스트한 결과 다음과 같이 측정되었다.

```text
===== Query Count: 11 =====
```

### Result

| 항목 | 결과 |
|---|---:|
| Reservation | 10 |
| Vehicle | 10 |
| Reservation 목록 조회 | 1 |
| 추가 Vehicle 조회 | 10 |
| Total Query Count | 11 |

예약 목록을 가져오는 Statement가 먼저 실행된 후,
각 Reservation의 Vehicle에 접근할 때 추가 SELECT가 발생하였다.

결과적으로 다음과 같은 구조가 되었다.

```text
Reservation 목록 조회
        │
        └── 1
             │
             ├── Vehicle 1  → +1
             ├── Vehicle 2  → +1
             ├── Vehicle 3  → +1
             ├── ...
             └── Vehicle 10 → +1
```

즉 테스트 데이터 10건에 대해:

```text
1 + 10 = 11
```

개의 Statement가 측정되었다.

---

## 6. Cause Analysis

`Reservation`에서 `Vehicle` 연관관계를 지연 로딩하고 있는 상태에서
예약 목록만 먼저 조회하면 Vehicle 데이터는 즉시 조회되지 않는다.

따라서 다음과 같이 Vehicle의 실제 데이터를 사용하는 순간
추가 조회가 필요해진다.

```java
reservation.getVehicle().getVehicleNumber();
```

예약 목록의 크기가 `N`이고,
각 Reservation이 서로 다른 Vehicle을 참조한다고 가정하면
최악의 경우 다음과 같은 형태로 Statement 수가 증가할 수 있다.

```text
1 + N
```

이번 테스트에서는 `N = 10`이므로 실제로:

```text
1 + 10 = 11
```

이 측정되었다.

이 구조에서는 예약 데이터가 증가할수록
연관된 Vehicle을 조회하기 위한 추가 데이터베이스 접근도 함께 증가할 수 있다.

---

## 7. Optimization

예약 목록 화면에서는 각 Reservation의 Vehicle 정보가 필요하므로
목록 조회 시 Vehicle을 함께 가져오도록 `@EntityGraph`를 적용하였다.

```java
@EntityGraph(attributePaths = "vehicle")
List<Reservation>
findAllByMemberIdOrderByReservationDateDescReservationTimeDesc(
        Long memberId
);
```

연관관계의 기본 Fetch 전략 자체를 `EAGER`로 변경하지 않고,
Vehicle 정보가 실제로 필요한 조회에서만 명시적으로 Fetch 전략을 지정하였다.

즉 다음과 같은 방식으로 전체 연관관계의 기본 정책을 변경하지 않는다.

```java
@ManyToOne(fetch = FetchType.EAGER)
```

대신 기본적으로 LAZY Loading을 유지하고,
Use Case에 따라 필요한 연관관계만 함께 조회하도록 구성하였다.

```text
Default
Reservation → Vehicle
LAZY

Reservation List Query
Reservation + Vehicle
@EntityGraph
```

이를 통해 특정 조회 요구사항에 맞게 Fetch 전략을 제어하면서
불필요한 전역 EAGER Loading을 방지한다.

---

## 8. After Optimization

`@EntityGraph`를 적용한 후
Before 테스트와 동일한 데이터 및 동일한 측정 방법으로 다시 테스트하였다.

테스트 결과:

```text
===== Query Count: 1 =====
```

### Result

| 항목 | 결과 |
|---|---:|
| Reservation | 10 |
| Vehicle | 10 |
| Total Query Count | 1 |
| 추가 Vehicle 조회 | 0 |

Reservation 목록을 조회할 때 필요한 Vehicle 데이터도 함께 조회되면서
Reservation별 추가 Vehicle SELECT가 발생하지 않았다.

---

## 9. Before / After Comparison

동일한 테스트 조건에서 측정한 결과는 다음과 같다.

| 항목 | Before | After |
|---|---:|---:|
| Member | 1 | 1 |
| Reservation | 10 | 10 |
| Vehicle | 10 | 10 |
| Query Count | 11 | 1 |
| 추가 Vehicle Query | 10 | 0 |
| N+1 | 발생 | 제거 |

Query Count 감소율은 다음과 같다.

```text
(11 - 1) / 11 × 100
≈ 90.9%
```

따라서 이번 테스트에서는:

```text
Before
11 Queries

    ↓ @EntityGraph

After
1 Query
```

로 감소하였다.

**동일한 테스트 조건에서 JDBC Statement 측정 횟수가 약 90.9% 감소하였다.**

---

## 10. Regression Test

N+1 문제가 향후 Repository 변경 과정에서 다시 발생하는 것을
자동으로 확인할 수 있도록 Query Count를 테스트에서 검증한다.

```java
long queryCount =
        statistics.getPrepareStatementCount();

assertThat(queryCount)
        .isEqualTo(1);
```

이를 통해 향후 Fetch 전략이 변경되어
추가 Statement가 발생할 경우 테스트에서 이를 감지할 수 있도록 한다.

성능 개선을 일회성 작업으로 끝내지 않고
회귀 테스트를 통해 현재 조회 구조를 지속적으로 검증한다.

---

## 11. Result

예약 목록 조회 과정에서 N+1 문제를 직접 재현하고,
Fetch 전략 변경 전후의 JDBC Statement 수를 비교하였다.

### Before

```text
Reservation : 10
Vehicle     : 10

Query Count : 11
```

### After

```text
Reservation : 10
Vehicle     : 10

Query Count : 1
```

최종적으로:

```text
N+1 재현
        ↓
11 Queries 확인
        ↓
원인 분석
        ↓
@EntityGraph 적용
        ↓
동일 조건 재측정
        ↓
1 Query 확인
```

과정을 통해 예약 목록 조회의 N+1 문제를 제거하였다.

---

## 12. Limitations

이번 테스트는 N+1 문제와 Fetch 전략에 따른
데이터베이스 접근 횟수 차이를 확인하기 위한 실험이다.

현재 테스트 환경은 다음과 같은 제한이 있다.

- H2 In-Memory Database 사용
- Reservation 10건 기준
- 단일 테스트 프로세스
- 실제 네트워크 통신 없음
- 실제 운영 트래픽 없음
- Query 실행 시간 비교 미수행
- PostgreSQL 또는 MySQL 실행계획 미확인

따라서 이번 결과에서 확인한 약 `90.9%`는
**JDBC Statement 측정 횟수의 감소율**이다.

이를 다음과 같이 해석해서는 안 된다.

```text
잘못된 해석

"서비스 성능이 90.9% 향상되었다."
"응답 시간이 90.9% 감소하였다."
```

현재 실험에서 확인할 수 있는 결과는 다음과 같다.

```text
확인된 결과

"동일한 테스트 조건에서
JDBC Statement 측정 횟수가
11회에서 1회로 감소하였다."
```

실제 운영 환경의 성능 개선 효과를 판단하기 위해서는
향후 운영 DB 환경에서 실행 시간과 실행계획을 별도로 측정해야 한다.

---

## 13. Future Work

향후 다음 데이터베이스 실험을 진행한다.

- Reservation 상세 조회 연관관계 분석
- ReservationItem 및 MaintenanceItem 조회 구조 확인
- 예약 날짜 및 상태 기반 Index 검토
- Index 적용 전후 조회 성능 비교
- `EXPLAIN ANALYZE` 기반 실행계획 비교
- 대량 예약 데이터 기반 Pagination 검증
- 동시 예약 요청 충돌 재현
- Unique Constraint를 통한 데이터 무결성 검증
- Lock 적용 전후 동시성 제어 비교
- PostgreSQL 또는 MySQL 환경에서 재측정

---

## 14. Performance Experiment Summary

| Experiment | Before | After | Status |
|---|---:|---:|---|
| Reservation List N+1 | 11 Queries | 1 Query | Completed |
| Reservation Detail Query | - | - | Planned |
| Reservation Index | - | - | Planned |
| Execution Plan | - | - | Planned |
| Pagination | - | - | Planned |
| Concurrent Reservation | - | - | Planned |
| Lock / Constraint | - | - | Planned |
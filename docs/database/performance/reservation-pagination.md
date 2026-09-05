# Reservation Pagination Performance

> GarageCare Database Performance Experiment  
> Status: PostgreSQL Pagination Analysis Completed  
> Last Updated: 2026-09-05

---

## 1. Overview

GarageCare의 회원별 예약 목록 조회에 Offset 기반 Pagination을 적용하고,
PostgreSQL 실행계획을 통해 페이지 깊이에 따른 조회 비용 변화를 분석한다.

기존 예약 목록 조회는 회원의 전체 예약 목록을 조회하는 구조였으며,
데이터가 증가할 경우 불필요하게 많은 Row를 애플리케이션으로 가져올 가능성이 있었다.

이를 개선하기 위해 Spring Data JPA의 `Pageable`을 적용하여
한 번에 제한된 개수의 예약만 조회하도록 변경하였다.

이번 실험에서는 단순히 Pagination 적용 여부만 확인하지 않고,

```text
OFFSET 0
OFFSET 1,000
OFFSET 5,000
OFFSET 9,000
```

에 대한 PostgreSQL 실행계획을 비교하여
Offset Pagination의 성능 특성과 한계를 확인하였다.

또한 `Page<T>` 사용 시 발생하는 Count Query의 실행계획도 함께 분석하였다.

---

## 2. Background

예약 목록 조회의 기존 성능 개선 흐름은 다음과 같다.

```text
Reservation List Query
        ↓
N+1 Problem
        ↓
@EntityGraph
        ↓
11 Statements → 1 Statement
        ↓
Composite Index
        ↓
PostgreSQL EXPLAIN ANALYZE
        ↓
Pagination
```

N+1 문제를 해결한 이후에도
회원의 예약 데이터가 계속 증가하면 전체 데이터를 한 번에 조회하는 방식에는 한계가 있다.

예를 들어 한 회원이 10,000개의 예약 데이터를 가지고 있을 때,
화면에서 실제 필요한 예약이 10건뿐이라면 전체 10,000건을 조회할 필요가 없다.

따라서 다음과 같이 Page 단위 조회를 적용하였다.

```text
Before

List<Reservation>

        ↓

모든 예약 조회


After

Page<Reservation>

        ↓

LIMIT / OFFSET 기반 조회
```

---

## 3. Pagination Strategy

현재 GarageCare에서는 Spring Data JPA의 `Pageable`을 이용한
Offset Pagination을 사용한다.

기본 페이지 크기는 다음과 같다.

```text
Page Size: 10
```

조회 정렬 기준은 기존 예약 목록 정책을 유지한다.

```text
reservation_date DESC
reservation_time DESC
```

이를 SQL로 표현하면 다음과 같다.

```sql
SELECT *
FROM reservations
WHERE member_id = ?
ORDER BY reservation_date DESC,
         reservation_time DESC
LIMIT 10
OFFSET ?;
```

---

## 4. Test Environment

### Database

```text
PostgreSQL 17.11
```

### Test Dataset

```text
Member: 1
Reservations: 10,000
Page Size: 10
```

모든 예약은 동일 회원의 데이터로 생성하였다.

이번 실험의 목적은 특정 회원의 예약 목록이 매우 커졌을 때
Offset 깊이에 따라 조회 비용이 어떻게 변하는지를 확인하는 것이다.

### Index

예약 조회에는 기존 Index 실험에서 검증한 다음 복합 인덱스를 유지하였다.

```sql
CREATE INDEX idx_reservation_member_date_time
ON reservations (
    member_id,
    reservation_date,
    reservation_time
);
```

---

## 5. Test Query

실행계획 측정에는 다음 명령을 사용하였다.

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT *
FROM reservations
WHERE member_id = ?
ORDER BY reservation_date DESC,
         reservation_time DESC
LIMIT 10
OFFSET ?;
```

비교 Offset:

```text
0
1,000
5,000
9,000
```

각 Query는 실제 10개의 Row만 반환하도록 동일한 `LIMIT 10`을 사용하였다.

따라서 반환 Row 수는 동일하지만
Offset 깊이가 증가하면서 내부적으로 처리해야 하는 Row가 어떻게 변하는지를 관찰하였다.

---

## 6. OFFSET 0

### Execution Plan

```text
Limit
  (actual time=0.008..0.013 rows=10)

    ↓

Index Scan Backward
using idx_reservation_member_date_time
  (actual time=0.007..0.011 rows=10)

Index Cond:
member_id = 71
```

### Buffers

```text
shared hit=11
```

### Execution Time

```text
0.020 ms
```

### Analysis

첫 페이지에서는 PostgreSQL이 복합 인덱스를 직접 활용하였다.

```text
idx_reservation_member_date_time
```

에 대해 `Index Scan Backward`가 수행되었으며,
정렬 조건이 인덱스 컬럼 순서와 호환되기 때문에 별도의 Sort 연산이 필요하지 않았다.

실제로 필요한 10개의 Row만 읽고 바로 조회를 종료하였다.

```text
Index Scan rows = 10
Returned rows   = 10
```

즉 첫 페이지 조회에서는 Offset Pagination의 비용이 매우 작다.

---

## 7. OFFSET 1,000

### Execution Plan

```text
Limit
  (actual time=0.244..0.245 rows=10)

    ↓

Index Scan Backward
using idx_reservation_member_date_time
  (actual time=0.004..0.187 rows=1010)
```

### Buffers

```text
shared hit=914
```

### Execution Time

```text
0.251 ms
```

### Analysis

Offset이 1,000으로 증가했지만
PostgreSQL은 여전히 복합 인덱스를 사용하였다.

그러나 최종적으로 반환되는 Row는 10개임에도
Index Scan에서는 총 1,010개의 Row를 읽었다.

```text
OFFSET 1,000
+
LIMIT 10

=

1,010 Rows processed
```

즉 PostgreSQL은 처음부터 1,001번째 Row로 이동하는 것이 아니라,
앞선 1,000개의 결과를 처리한 뒤 버리고
그 다음 10개를 반환한다.

이것이 Offset Pagination의 핵심 비용이다.

실행시간도 첫 페이지에 비해 증가하였다.

```text
OFFSET 0
0.020 ms

↓

OFFSET 1,000
0.251 ms
```

이번 단일 실행 기준으로 약 12.5배 증가하였다.

단, 실행시간은 캐시 상태와 시스템 환경에 영향을 받을 수 있으므로
절대적인 성능 비율로 일반화하지 않는다.

---

## 8. OFFSET 5,000

### Execution Plan

```text
Limit
  (actual time=0.863..0.864 rows=10)

    ↓

Index Scan Backward
using idx_reservation_member_date_time
  (actual time=0.005..0.743 rows=5010)
```

### Buffers

```text
shared hit=4528
```

### Execution Time

```text
0.878 ms
```

### Analysis

Offset이 5,000이 되자
PostgreSQL은 5,010개의 Row를 처리해야 했다.

```text
OFFSET 5,000
+
LIMIT 10

=

5,010 Rows processed
```

최종 반환 데이터는 여전히 10건이지만,
앞의 5,000건은 실제 사용자에게 반환되지 않는다.

버퍼 접근 수도 크게 증가하였다.

```text
OFFSET 0
shared hit = 11

OFFSET 1,000
shared hit = 914

OFFSET 5,000
shared hit = 4,528
```

실행시간도 증가하였다.

```text
0.020 ms
    ↓
0.251 ms
    ↓
0.878 ms
```

이 결과는 Offset 깊이가 증가하면서
Index를 사용하더라도 앞선 Row를 탐색해야 하는 비용이 누적된다는 것을 보여준다.

---

## 9. OFFSET 9,000

### Execution Plan

```text
Limit
  (actual time=1.777..1.778 rows=10)

    ↓

Sort
  (actual time=1.457..1.632 rows=9010)

    ↓

Seq Scan
  (actual time=0.014..0.566 rows=10000)
```

### Sort

```text
Sort Key:
reservation_date DESC,
reservation_time DESC

Sort Method:
quicksort

Memory:
1088kB
```

### Buffers

```text
shared hit=94
```

### Execution Time

```text
1.785 ms
```

### Analysis

OFFSET 9,000에서는 중요한 변화가 발생하였다.

앞선 실험에서는 PostgreSQL이 다음 실행계획을 사용했다.

```text
Index Scan Backward
```

하지만 OFFSET 9,000에서는 PostgreSQL이 인덱스 사용을 포기하고 다음 계획을 선택하였다.

```text
Seq Scan
    ↓
Sort
    ↓
Limit
```

전체 예약 10,000건을 Sequential Scan으로 읽은 뒤,
정렬하고 9,000건을 건너뛴 후 최종 10건을 반환하였다.

```text
Seq Scan rows = 10,000

↓

Sort rows = 9,010

↓

Returned rows = 10
```

이는 매우 중요한 결과이다.

복합 인덱스가 존재한다고 해서
모든 Pagination Query에서 PostgreSQL이 항상 해당 인덱스를 사용하는 것은 아니다.

Optimizer는 Query 비용을 계산한 뒤
깊은 Offset에서는 Index Scan으로 9,010개의 Row를 따라가는 것보다

```text
전체 Table Scan
+
Sort
```

가 더 저렴하다고 판단하였다.

즉,

```text
Index Exists
≠
Always Index Scan
```

이라는 점을 실제 실행계획을 통해 확인하였다.

---

## 10. Offset Comparison

실험 결과를 정리하면 다음과 같다.

| Offset | Scan Strategy | Processed Rows | Buffers | Execution Time |
|---:|---|---:|---:|---:|
| 0 | Index Scan Backward | 10 | 11 hits | 0.020 ms |
| 1,000 | Index Scan Backward | 1,010 | 914 hits | 0.251 ms |
| 5,000 | Index Scan Backward | 5,010 | 4,528 hits | 0.878 ms |
| 9,000 | Seq Scan + Sort | 10,000 scan / 9,010 sort | 94 hits | 1.785 ms |

실행시간은 이번 단일 테스트 실행에서 측정된 값이며,
캐시 상태 및 실행 환경에 따라 달라질 수 있다.

따라서 절대적인 성능 수치보다는
Offset 증가에 따른 실행계획과 처리 Row 변화에 초점을 둔다.

---

## 11. Execution Time Trend

이번 실행에서는 다음과 같은 변화가 나타났다.

```text
OFFSET 0
0.020 ms

↓

OFFSET 1,000
0.251 ms

↓

OFFSET 5,000
0.878 ms

↓

OFFSET 9,000
1.785 ms
```

첫 페이지와 OFFSET 9,000을 비교하면
이번 실행 기준 약 89배의 실행시간 차이가 발생하였다.

하지만 이 수치는 하나의 측정 결과이므로

```text
"Offset Pagination은 항상 89배 느리다"
```

와 같이 일반화할 수 없다.

중요한 사실은 실행시간 수치 자체보다
PostgreSQL이 실제로 처리한 Row의 수가 Offset과 함께 증가했다는 점이다.

---

## 12. Why Deep Offset Is Expensive

Offset Pagination은 다음과 같이 동작한다.

```text
LIMIT 10
OFFSET 0

10 Rows 처리
↓
10 Rows 반환
```

하지만 깊은 페이지에서는:

```text
LIMIT 10
OFFSET 5,000

5,010 Rows 처리
↓
앞의 5,000 Rows 폐기
↓
10 Rows 반환
```

형태가 된다.

즉 Offset은 조회 시작 지점으로 직접 이동하는 포인터가 아니다.

PostgreSQL은 정렬 조건에 맞는 결과를 순서대로 처리한 뒤
Offset만큼의 결과를 버려야 한다.

따라서 반환되는 데이터 수가 항상 10건이라 하더라도
페이지가 뒤로 갈수록 조회 비용은 증가할 수 있다.

---

## 13. Index Interaction

이번 Pagination 실험은
이전에 적용한 복합 인덱스의 효과도 다시 확인할 수 있었다.

복합 인덱스:

```text
(member_id, reservation_date, reservation_time)
```

초기 Offset에서는 다음과 같이 사용되었다.

```text
Index Scan Backward
using idx_reservation_member_date_time
```

이 인덱스는 다음 두 조건을 동시에 지원한다.

```text
WHERE member_id = ?

ORDER BY
reservation_date DESC,
reservation_time DESC
```

따라서 초기 페이지에서는 별도의 Sort 없이
인덱스 순서를 역방향으로 읽어 필요한 결과를 빠르게 반환할 수 있었다.

그러나 Offset이 지나치게 깊어지면
Optimizer는 다른 계획을 선택하였다.

```text
OFFSET 9,000

Index Scan
    X

Seq Scan + Sort
    O
```

따라서 인덱스 최적화와 Pagination 최적화는
서로 독립적인 문제로 볼 필요가 있다.

---

## 14. Count Query

Spring Data JPA의 `Page<T>`는
현재 페이지 데이터뿐 아니라 전체 페이지 수를 제공해야 한다.

이를 위해 일반적으로 Content Query와 별도로 Count Query가 실행된다.

실험 Query:

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT COUNT(*)
FROM reservations
WHERE member_id = ?;
```

### Execution Plan

```text
Aggregate
  (actual time=0.708..0.708 rows=1)

    ↓

Seq Scan
  (actual time=0.003..0.310 rows=10000)
```

### Buffers

```text
shared hit=94
```

### Execution Time

```text
0.714 ms
```

---

## 15. Count Query Analysis

Count Query에서는 PostgreSQL이 복합 인덱스를 사용하지 않고
전체 `reservations` 테이블을 Sequential Scan하였다.

```text
Seq Scan rows = 10,000
```

현재 테스트 데이터에서는 모든 10,000개의 예약이
동일한 회원의 데이터이다.

즉 조건의 Selectivity가 다음과 같다.

```text
10,000 / 10,000
= 100%
```

이 경우 거의 모든 Row를 읽어야 하므로
인덱스를 따라가는 것보다 Sequential Scan이 더 저렴하다고
Optimizer가 판단한 것으로 볼 수 있다.

따라서 다음 결과 역시 정상적인 PostgreSQL Optimizer의 판단이다.

```text
Composite Index exists

하지만

COUNT Query
→ Seq Scan
```

---

## 16. Page<T> Cost

현재 GarageCare에서는 `Page<Reservation>`을 사용한다.

이를 단순화하면 다음과 같다.

```text
Page<T>

Content Query
+
Count Query
```

첫 페이지 조회를 예로 들면 이번 실험에서:

```text
Content Query
Execution Time: 0.020 ms

Count Query
Execution Time: 0.714 ms
```

였다.

즉 데이터 10건을 가져오는 Query보다
전체 데이터 개수를 계산하는 Count Query의 비용이 더 크게 나타났다.

단, 이 역시 현재 테스트 데이터 분포에서 측정된 결과이다.

그러나 데이터 규모가 커질수록
Pagination의 Content Query뿐 아니라
Count Query 또한 고려해야 한다는 점을 확인할 수 있었다.

---

## 17. Page vs Slice

현재는 다음 요구사항을 제공하기 위해 `Page<T>`를 사용한다.

```text
현재 페이지
전체 페이지 수
전체 데이터 수
다음 페이지 여부
이전 페이지 여부
```

하지만 전체 데이터 개수가 필요하지 않은 화면에서는
향후 `Slice<T>`를 사용할 수 있다.

개념적으로:

```text
Page<T>

Content Query
+
Count Query
```

반면:

```text
Slice<T>

다음 페이지 존재 여부를 판단할 만큼만 조회
Count Query 불필요
```

구조를 사용할 수 있다.

GarageCare의 현재 규모에서는 즉시 변경할 필요는 없지만,
향후 데이터 증가 시 Count Query 비용이 문제가 된다면
검토할 수 있는 최적화 방법이다.

---

## 18. Offset Pagination Limitation

이번 실험에서 Offset Pagination의 핵심 한계가 명확하게 확인되었다.

### Small Offset

```text
OFFSET 0

Index Scan
↓
10 Rows
↓
Fast
```

### Medium Offset

```text
OFFSET 5,000

Index Scan
↓
5,010 Rows 처리
↓
10 Rows 반환
```

### Deep Offset

```text
OFFSET 9,000

Seq Scan
↓
10,000 Rows
↓
Sort
↓
9,000 Rows Skip
↓
10 Rows 반환
```

따라서 Offset Pagination은 구현이 간단하고
페이지 번호 기반 UI를 제공하기 쉽다는 장점이 있지만,
깊은 페이지에서는 비용이 증가할 수 있다.

---

## 19. Alternative: Keyset Pagination

Offset Pagination의 대안으로 Keyset Pagination을 사용할 수 있다.

예를 들어 현재 정렬 기준이:

```text
reservation_date DESC
reservation_time DESC
```

이라면 마지막으로 조회한 예약의 값을 기준으로 다음 페이지를 조회할 수 있다.

개념적으로:

```sql
SELECT *
FROM reservations
WHERE member_id = ?
  AND (
      reservation_date < ?
      OR (
          reservation_date = ?
          AND reservation_time < ?
      )
  )
ORDER BY reservation_date DESC,
         reservation_time DESC
LIMIT 10;
```

이 방식은 깊은 Offset을 건너뛰지 않기 때문에
대규모 데이터에서 더 안정적인 조회 성능을 기대할 수 있다.

다만 페이지 번호 기반 임의 이동이 어렵고,
정렬 기준에 동점 데이터가 존재할 경우 안정적인 순서를 위해
추가적인 Tie-breaker가 필요하다.

예를 들어:

```text
reservation_date DESC
reservation_time DESC
id DESC
```

같은 방식이다.

현재 GarageCare에서는 아직 Keyset Pagination을 적용하지 않는다.

이번 실험의 목적은 Offset Pagination을 적용하고
그 성능 특성과 한계를 직접 확인하는 것이기 때문이다.

---

## 20. Conclusion

이번 실험을 통해 GarageCare 예약 목록 조회에
Offset Pagination을 적용하고 PostgreSQL 실행계획을 분석하였다.

실험 결과:

```text
OFFSET 0
Index Scan
10 Rows 처리
Execution Time 0.020 ms

OFFSET 1,000
Index Scan
1,010 Rows 처리
Execution Time 0.251 ms

OFFSET 5,000
Index Scan
5,010 Rows 처리
Execution Time 0.878 ms

OFFSET 9,000
Seq Scan + Sort
10,000 Rows Scan
Execution Time 1.785 ms
```

를 확인하였다.

Offset이 증가할수록
최종 반환 Row는 동일한 10건이지만
내부적으로 처리해야 하는 Row 수가 증가하였다.

또한 OFFSET 9,000에서는 PostgreSQL Optimizer가
기존 복합 인덱스를 사용하는 대신

```text
Seq Scan
+
Sort
```

를 선택하였다.

이를 통해 다음 사실을 확인하였다.

```text
1. Pagination은 반환 데이터 수를 제한한다.

2. Offset Pagination은 깊은 페이지에서
   앞선 Row를 처리한 뒤 폐기하는 비용이 발생한다.

3. Index가 존재한다고 해서
   PostgreSQL이 모든 Query에서 항상 Index Scan을 선택하지 않는다.

4. Optimizer는 데이터 분포와 Query 비용을 기반으로
   실행계획을 변경한다.

5. Page<T>는 Content Query 외에도
   Count Query 비용을 발생시킬 수 있다.
```

현재 GarageCare의 데이터 규모와
페이지 번호 기반 UI 요구사항을 고려하여
Offset Pagination을 유지한다.

다만 향후 예약 데이터가 크게 증가하고
깊은 페이지 조회가 실제 성능 문제로 확인될 경우,

```text
Page → Slice

Offset Pagination → Keyset Pagination
```

을 단계적으로 검토할 수 있다.

---

## 21. Performance Optimization Flow

이번 실험까지 포함한 GarageCare 예약 목록 조회 최적화 흐름은 다음과 같다.

```text
Reservation List
        ↓
N+1 Detection
        ↓
@EntityGraph
        ↓
11 JDBC Statements
→ 1 JDBC Statement
        ↓
Composite Index
(member_id, reservation_date, reservation_time)
        ↓
H2 Execution Plan Analysis
        ↓
PostgreSQL Revalidation
        ↓
Seq Scan
→ Bitmap Index Scan
        ↓
Offset Pagination
        ↓
Page Size 10
        ↓
Deep Offset Cost Analysis
        ↓
Count Query Analysis
```

이를 통해 단순히 Pagination 기능을 적용하는 것에서 끝내지 않고,

```text
기능 구현
→ SQL 확인
→ 실행계획 분석
→ 성능 측정
→ 한계 확인
→ 개선 대안 검토
```

의 전체 과정을 경험하였다.

---

## 22. Decision

현재 GarageCare에서는 다음 구조를 유지한다.

```text
Spring Data JPA Pageable
+
Page<T>
+
Offset Pagination
+
Composite Index
```

이유:

```text
- 현재 서비스 규모에서는 충분히 빠르다.
- 구현이 단순하다.
- 페이지 번호 기반 UI 제공이 쉽다.
- PostgreSQL 실행계획을 통해 실제 동작을 검증하였다.
```

현재 단계에서는 성급한 Keyset Pagination 도입보다
운영 환경 구축 및 실제 배포를 우선한다.

향후 실제 운영 데이터에서 성능 문제가 관찰될 경우
측정 결과를 기반으로 Pagination 전략을 다시 결정한다.

---

## 23. Related

### Parent

```text
docs/database/database-performance.md
```

### Previous Experiments

```text
docs/database/performance/reservation-n-plus-one.md
docs/database/performance/reservation-index.md
docs/database/postgresql-migration.md
```

### Current Experiment

```text
docs/database/performance/reservation-pagination.md
```

### Next Phase

```text
Deployment & Infrastructure
```

### Related Source

```text
src/main/java/com/hyunu/garagecare/reservation/
src/test/java/com/hyunu/garagecare/reservation/repository/
```
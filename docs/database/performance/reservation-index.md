# Reservation Index & Execution Plan Analysis

> GarageCare Database Performance Experiment  
> Status: H2 Analysis Completed / PostgreSQL Revalidation Planned  
> Last Updated: 2026-08-28

---

## 1. Overview

GarageCare의 회원별 예약 목록 조회 Query를 대상으로
인덱스 구조와 실행계획을 분석한다.

이전 N+1 실험에서는 예약 목록 조회 과정에서 발생하던
불필요한 JDBC Statement 수를 다음과 같이 개선하였다.

```text
Before
11 Statements

    ↓ @EntityGraph

After
1 Statement
```

하지만 Query 수가 1개로 감소했다고 해서
해당 Query 자체가 데이터베이스 내부에서 효율적으로 실행된다는 의미는 아니다.

따라서 이번 실험에서는 남은 예약 목록 Query가
어떤 인덱스를 사용하고 어떤 실행계획으로 처리되는지 확인한다.

전체 과정은 다음과 같다.

```text
조회 Query 분석
→ 기존 Index 확인
→ Before 실행계획 확인
→ 복합 Index 설계
→ Index 생성 확인
→ 테스트 데이터 개선
→ ANALYZE
→ After 실행계획 확인
→ 결과 분석
```

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

최종 실험 데이터는 다음과 같이 구성하였다.

```text
Member              : 10
Reservation / Member: 1,000
Total Reservation   : 10,000
Target Member       : 1
Target Reservation  : 1,000
```

H2 환경에서 먼저 인덱스 선택과 실행계획을 확인하고,
실제 운영 DBMS 전환 후 동일 조건으로 재검증한다.

---

## 3. Target Query

현재 예약 목록 Repository의 주요 조회 메서드는 다음과 같다.

```java
findAllByMemberIdOrderByReservationDateDescReservationTimeDesc(
        Long memberId
);
```

이를 SQL 관점에서 단순화하면 다음과 같다.

```sql
SELECT *
FROM reservations
WHERE member_id = ?
ORDER BY reservation_date DESC,
         reservation_time DESC;
```

Query의 주요 조건은 다음과 같다.

```text
WHERE
→ member_id

ORDER BY
→ reservation_date DESC
→ reservation_time DESC
```

따라서 다음 복합 인덱스를 후보로 선정하였다.

```text
(member_id, reservation_date, reservation_time)
```

목표는 `member_id` 조건 검색뿐 아니라
예약 날짜와 시간 정렬까지 고려한 인덱스가
실제 실행계획에서 선택되는지 확인하는 것이다.

---

## 4. Before Measurement

복합 인덱스를 추가하기 전
H2의 `EXPLAIN`을 이용하여 실행계획을 확인하였다.

```sql
EXPLAIN
SELECT *
FROM reservations
WHERE member_id = ?
ORDER BY reservation_date DESC,
         reservation_time DESC;
```

결과:

```text
FROM "PUBLIC"."RESERVATIONS"
    /* PUBLIC.FKI17VAAPHGBNWR0TBRG0QU0Q66_INDEX_4: MEMBER_ID = ?1 */
WHERE "MEMBER_ID" = ?1
ORDER BY 1 DESC, 2 DESC
```

H2는 이미 다음 인덱스를 사용하고 있었다.

```text
FKI17VAAPHGBNWR0TBRG0QU0Q66_INDEX_4
→ MEMBER_ID
```

### Before Result

| 항목 | 결과 |
|---|---|
| Query Condition | `member_id = ?` |
| Sort Condition | `reservation_date DESC, reservation_time DESC` |
| Selected Index | MEMBER_ID FK Index |
| Composite Index | 없음 |

처음에는 별도의 `@Index`를 정의하지 않았으므로
적절한 인덱스가 없을 것으로 예상하였다.

하지만 실제 실행계획에서는
`member_id` Foreign Key와 관련된 인덱스가 이미 사용되고 있었다.

---

## 5. Existing Index Analysis

실제 인덱스 상태를 확인하기 위해
H2 Metadata를 조회하였다.

```sql
SELECT
    INDEX_NAME,
    COLUMN_NAME
FROM INFORMATION_SCHEMA.INDEX_COLUMNS
WHERE TABLE_NAME = 'RESERVATIONS'
ORDER BY INDEX_NAME,
         ORDINAL_POSITION;
```

Before 상태에서는 다음과 같은 주요 인덱스가 존재하였다.

```text
FK7S1RLT0CCV2HJEAUBF52DI8ER_INDEX_4
→ VEHICLE_ID

FKI17VAAPHGBNWR0TBRG0QU0Q66_INDEX_4
→ MEMBER_ID

PRIMARY_KEY_4
→ ID
```

이를 통해 실험 방향을 다음과 같이 수정하였다.

```text
초기 예상

Index 없음
    ↓
Composite Index 추가
```

실제 상황:

```text
기존 MEMBER_ID Index
    ↓
Composite Index 추가
    ↓
Optimizer 선택 비교
```

즉 이번 실험은 **인덱스 유무 비교가 아니라
기존 단일 인덱스와 복합 인덱스의 선택 비교**가 되었다.

---

## 6. Initial Test Data Problem

초기 테스트에서는 한 명의 회원에게
Reservation 10,000건을 생성하였다.

```text
Member 1
└── Reservation × 10,000
```

하지만 대상 Query는 다음 조건을 사용한다.

```sql
WHERE member_id = ?
```

전체 Reservation이 동일한 Member를 참조하면
조건 조회 결과가 전체 데이터와 거의 동일해진다.

```text
Total Reservation : 10,000
Target Reservation: 10,000

조회 대상 비율    : 100%
```

이 데이터 구조는 `member_id` 조건의 인덱스 선택을 비교하기에
적절하지 않다고 판단하였다.

---

## 7. Test Data Improvement

실제 서비스에 가까운 데이터 분포를 만들기 위해
다음과 같이 데이터를 변경하였다.

```text
Member 1  → Reservation 1,000
Member 2  → Reservation 1,000
Member 3  → Reservation 1,000
...
Member 10 → Reservation 1,000

Total     → Reservation 10,000
```

특정 Member 한 명만 조회하면:

```text
전체 Reservation : 10,000
조회 Reservation : 1,000
```

이 된다.

데이터 생성 후 H2가 현재 데이터 분포를 반영할 수 있도록
다음 명령도 실행하였다.

```sql
ANALYZE;
```

테스트 코드에서는 다음과 같이 수행하였다.

```java
entityManager
        .createNativeQuery("ANALYZE")
        .executeUpdate();
```

---

## 8. Composite Index

실제 Query 패턴을 기반으로
다음 복합 인덱스를 추가하였다.

```text
IDX_RESERVATION_MEMBER_DATE_TIME

MEMBER_ID
RESERVATION_DATE
RESERVATION_TIME
```

JPA Entity에는 다음과 같이 정의하였다.

```java
@Table(
        name = "reservations",
        indexes = {
                @Index(
                        name = "idx_reservation_member_date_time",
                        columnList =
                                "member_id, reservation_date, reservation_time"
                )
        }
)
```

이후 H2 Metadata를 다시 조회하여
복합 인덱스가 실제로 생성되었는지 확인하였다.

```text
IDX_RESERVATION_MEMBER_DATE_TIME
→ MEMBER_ID
→ RESERVATION_DATE
→ RESERVATION_TIME
```

따라서 복합 인덱스 생성 자체는 정상적으로 완료되었다.

---

## 9. After Measurement

복합 인덱스를 생성하고,
테스트 데이터를 여러 Member에게 분산한 뒤
`ANALYZE`를 수행하였다.

이후 동일한 Query의 실행계획을 다시 확인하였다.

```sql
EXPLAIN
SELECT *
FROM reservations
WHERE member_id = ?
ORDER BY reservation_date DESC,
         reservation_time DESC;
```

결과:

```text
FROM "PUBLIC"."RESERVATIONS"
    /* PUBLIC.FKI17VAAPHGBNWR0TBRG0QU0Q66_INDEX_4: MEMBER_ID = ?1 */
WHERE "MEMBER_ID" = ?1
ORDER BY 1 DESC, 2 DESC
```

예상과 달리 H2 Optimizer는 새로 생성한:

```text
IDX_RESERVATION_MEMBER_DATE_TIME
```

복합 인덱스를 선택하지 않았다.

대신 Before와 동일하게:

```text
FKI17VAAPHGBNWR0TBRG0QU0Q66_INDEX_4
→ MEMBER_ID
```

기존 인덱스를 계속 사용하였다.

---

## 10. Before / After Comparison

| 항목 | Before | After |
|---|---|---|
| Total Reservation | 10,000 | 10,000 |
| Test Data Distribution | 초기 단일 Member 구조 | 10 Members × 1,000 |
| MEMBER_ID Index | 존재 | 존재 |
| Composite Index | 없음 | 존재 |
| Composite Index Columns | - | MEMBER_ID, DATE, TIME |
| ANALYZE | 미적용 | 적용 |
| Selected Index | MEMBER_ID FK Index | MEMBER_ID FK Index |
| Execution Plan Change | 기준 | 변화 없음 |

복합 인덱스는 정상적으로 존재하지만,
H2 실행계획에서는 사용되지 않았다.

따라서 현재 H2 환경에서는
복합 인덱스 적용에 따른 실행계획 개선을 확인하지 못하였다.

---

## 11. Problem-Solving Process

이번 실험은 예상과 다른 결과를 분석하는 과정이 핵심이었다.

### Step 1. 실제 Query 확인

회원별 예약 목록 조회의 조건과 정렬을 확인하였다.

```text
WHERE member_id
ORDER BY reservation_date, reservation_time
```

### Step 2. Before 실행계획 확인

`EXPLAIN`을 통해 이미 `MEMBER_ID` 인덱스가 사용되는 것을 발견하였다.

```text
FKI17VA...INDEX_4
→ MEMBER_ID
```

### Step 3. 실제 인덱스 Metadata 확인

`INFORMATION_SCHEMA.INDEX_COLUMNS`를 조회하여
기존 인덱스가 실제로 존재함을 확인하였다.

### Step 4. 복합 인덱스 설계

Query 패턴을 기반으로:

```text
(MEMBER_ID, RESERVATION_DATE, RESERVATION_TIME)
```

복합 인덱스를 후보로 선정하였다.

### Step 5. 테스트 데이터 문제 발견

초기 데이터가:

```text
1 Member × 10,000 Reservations
```

구조라서 `member_id` 조건의 데이터 분포가 부적절함을 확인하였다.

### Step 6. 데이터 분포 개선

다음과 같이 변경하였다.

```text
10 Members × 1,000 Reservations
```

### Step 7. Statistics 갱신

```sql
ANALYZE;
```

를 실행하였다.

### Step 8. 복합 인덱스 생성 확인

Metadata를 통해 다음 인덱스가 정상적으로 존재함을 확인하였다.

```text
IDX_RESERVATION_MEMBER_DATE_TIME
```

### Step 9. 동일 Query 재검증

`EXPLAIN`을 다시 실행하였다.

하지만 H2 Optimizer는 기존 MEMBER_ID 인덱스를 계속 선택하였다.

### Step 10. 결과를 강제로 성공으로 해석하지 않음

복합 인덱스가 존재한다는 사실만으로
성능이 개선되었다고 판단하지 않았다.

실제로 확인한 결과만 기록하고
운영 DBMS에서 재검증하기로 결정하였다.

---

## 12. Result

이번 H2 실험에서 확인한 내용은 다음과 같다.

```text
Query 분석
    ↓
기존 MEMBER_ID Index 발견
    ↓
Composite Index 설계
    ↓
Index 생성 확인
    ↓
테스트 데이터 개선
    ↓
ANALYZE
    ↓
EXPLAIN 재실행
    ↓
기존 MEMBER_ID Index 계속 선택
```

핵심적으로 확인한 내용은 다음과 같다.

- 인덱스를 직접 정의하지 않아도 DB에 기존 인덱스가 존재할 수 있다.
- 인덱스 생성 여부와 실제 Optimizer의 인덱스 선택은 별개의 문제이다.
- 복합 인덱스를 추가했다고 반드시 해당 인덱스가 사용되는 것은 아니다.
- 인덱스 실험에서는 데이터 개수뿐 아니라 데이터 분포도 중요하다.
- 예상과 다른 실행계획이 나오면 Metadata와 실제 실행계획을 함께 확인해야 한다.
- 성능 개선이 검증되지 않았다면 개선되었다고 기록하지 않아야 한다.

---

## 13. Limitations

이번 실험에는 다음 한계가 있다.

- H2 In-Memory Database 사용
- H2 Optimizer 기준 실행계획
- 실제 운영 DBMS가 아님
- `EXPLAIN` 중심의 분석
- PostgreSQL `EXPLAIN ANALYZE` 미수행
- 실제 DB 내부 실행시간 비교 미수행
- 실제 네트워크 지연 없음
- 실제 운영 데이터 분포와 차이가 있음

특히 H2와 PostgreSQL은
Optimizer와 통계정보 처리 방식이 다를 수 있다.

따라서 H2의 실행계획 결과를
실제 운영 DBMS의 성능 결과로 일반화하지 않는다.

---

## 14. Decision

현재 복합 인덱스:

```text
(MEMBER_ID, RESERVATION_DATE, RESERVATION_TIME)
```

는 H2 환경에서 생성 자체는 확인되었지만
실제 실행계획에서 선택되지 않았다.

따라서 현재 결과만으로:

```text
복합 인덱스 적용
→ 성능 개선
```

이라고 결론 내리지 않는다.

복합 인덱스의 실제 적용 여부는
PostgreSQL에서 동일한 Query와 데이터 분포를 기반으로
다시 검증한 뒤 결정한다.

---

## 15. PostgreSQL Revalidation

## 15. PostgreSQL Revalidation

H2 환경에서는 복합 인덱스가 정상적으로 생성되었지만,
Optimizer가 기존 `MEMBER_ID` Foreign Key 인덱스를 계속 선택하였다.

따라서 동일한 Query와 테스트 데이터 분포를 PostgreSQL에서 다시 구성하고,
`EXPLAIN (ANALYZE, BUFFERS)`를 이용하여 실행계획을 재검증하였다.

### Test Environment

```text
Database            : PostgreSQL 17.11
Member              : 10
Reservation / Member: 1,000
Total Reservation   : 10,000
Target Reservation  : 1,000
```

대상 Query는 H2 실험과 동일하게 유지하였다.

```sql
SELECT *
FROM reservations
WHERE member_id = ?
ORDER BY reservation_date DESC,
         reservation_time DESC;
```

실행계획은 다음 명령으로 확인하였다.

```sql
EXPLAIN (ANALYZE, BUFFERS)
```

---

### 15.1 Before Index

복합 인덱스를 제거한 상태에서 실행계획을 측정하였다.

```text
Sort
  Sort Key: reservation_date DESC, reservation_time DESC
  Sort Method: quicksort
  Buffers: shared hit=94

  -> Seq Scan on reservations
       Filter: member_id = ?
       Rows Removed by Filter: 9000
       Buffers: shared hit=94

Planning Time: 0.315 ms
Execution Time: 0.363 ms
```

PostgreSQL은 전체 Reservation 10,000건을 Sequential Scan으로 읽은 뒤,
조건에 맞지 않는 9,000건을 제거하였다.

남은 1,000건은:

```text
reservation_date DESC
reservation_time DESC
```

조건에 따라 별도로 정렬되었다.

실행 흐름은 다음과 같다.

```text
Reservations 10,000
        ↓
Sequential Scan
        ↓
9,000 Rows 제거
        ↓
1,000 Rows
        ↓
Sort
```

---

### 15.2 After Index

다음 복합 인덱스를 생성하였다.

```sql
CREATE INDEX idx_reservation_member_date_time
ON reservations (
    member_id,
    reservation_date,
    reservation_time
);
```

인덱스 생성 후:

```sql
ANALYZE reservations;
```

를 수행하고 동일한 Query를 다시 실행하였다.

실행계획은 다음과 같이 변경되었다.

```text
Sort
  Sort Key: reservation_date DESC, reservation_time DESC
  Sort Method: quicksort
  Buffers: shared hit=10 read=6

  -> Bitmap Heap Scan on reservations
       Recheck Cond: member_id = ?

       -> Bitmap Index Scan
            on idx_reservation_member_date_time
            Index Cond: member_id = ?

Planning Time: 0.094 ms
Execution Time: 0.184 ms
```

PostgreSQL Optimizer가 새로 생성한 복합 인덱스를
실제 실행계획에 사용하였다.

실행 흐름은 다음과 같이 변경되었다.

```text
Composite Index
        ↓
Bitmap Index Scan
        ↓
Bitmap Heap Scan
        ↓
1,000 Rows
        ↓
Sort
```

---

### 15.3 Before / After Comparison

| Metric | Before | After |
|---|---|---|
| Scan Type | Sequential Scan | Bitmap Index Scan + Bitmap Heap Scan |
| Total Rows | 10,000 | 10,000 |
| Result Rows | 1,000 | 1,000 |
| Rows Removed by Filter | 9,000 | - |
| Sort | Quicksort | Quicksort |
| Buffer | 94 hit | 10 hit + 6 read |
| Planning Time | 0.315 ms | 0.094 ms |
| Execution Time | 0.363 ms | 0.184 ms |
| Composite Index Used | No | Yes |

해당 실행에서는 Execution Time이:

```text
0.363 ms
    ↓
0.184 ms
```

로 감소하였다.

감소율은 약:

```text
49.3%
```

이다.

다만 실행시간과 Buffer 사용량은
DB Cache와 시스템 상태에 영향을 받을 수 있으므로,
단일 실행 결과를 일반적인 성능 향상률로 확정하지 않는다.

이번 실험에서 확실하게 확인한 결과는
PostgreSQL Optimizer가 복합 인덱스를 실제 실행계획에 사용했다는 점이다.

---

## 16. PostgreSQL Result Analysis

PostgreSQL에서는 H2와 달리
복합 인덱스가 실제 실행계획에 사용되었다.

### H2

```text
Existing MEMBER_ID Index
        ↓
Composite Index 생성
        ↓
Optimizer는 기존 Index 선택
```

### PostgreSQL

```text
Before
Sequential Scan

        ↓ Composite Index

After
Bitmap Index Scan
+
Bitmap Heap Scan
```

이를 통해 H2에서 확인하지 못했던
복합 인덱스의 실제 활용 여부를 PostgreSQL에서 확인할 수 있었다.

### Sort가 남아 있는 이유

복합 인덱스에는 다음 컬럼이 포함되어 있다.

```text
member_id
reservation_date
reservation_time
```

하지만 PostgreSQL은 이번 실행에서 일반적인 Index Scan이 아니라
Bitmap Index Scan을 선택하였다.

Bitmap Scan은 여러 Row의 위치를 효율적으로 찾는 데 유리하지만,
인덱스의 정렬 순서를 그대로 유지하지 않는다.

따라서 실행계획에는 여전히:

```text
Sort
→ reservation_date DESC
→ reservation_time DESC
```

단계가 남았다.

현재 조회 대상은 전체 데이터의 약 10%이다.

```text
1,000 / 10,000
= 10%
```

이 데이터 분포에서 PostgreSQL Optimizer는
Bitmap Scan 이후 정렬하는 실행계획을 선택하였다.

따라서 특정 실행계획을 강제로 유도하기보다
Optimizer가 실제 비용을 기반으로 선택한 결과를 그대로 기록한다.

### Index Decision

PostgreSQL에서는 다음 복합 인덱스가 실제 실행계획에 사용되었다.

```text
idx_reservation_member_date_time
(member_id, reservation_date, reservation_time)
```

따라서 현재 예약 목록 조회 패턴에서는
해당 복합 인덱스를 유지하기로 결정하였다.

```text
H2
→ 효과 확인 불가

PostgreSQL
→ 실제 Index 사용 확인

        ↓

Composite Index 유지
```
---

## 17. Connection with N+1 Experiment

이전 N+1 실험에서는 예약 목록 조회에서 발생하던
DB 접근 횟수를 줄였다.

```text
Reservation List N+1

11 Statements
        ↓
@EntityGraph
        ↓
1 Statement
```

이번 인덱스 실험에서는
그렇게 남은 Query가 DB 내부에서 어떻게 처리되는지를 분석하였다.

```text
N+1 Optimization

Application
    ↓
DB 접근 횟수 감소

11 → 1

        ↓

Index / Execution Plan

Database
    ↓
남은 Query의 실행 방식 분석
```

따라서 두 실험은 다음과 같이 연결된다.

```text
Reservation List
        ↓

N+1 Analysis
        ↓
11 Statements 발견
        ↓
@EntityGraph
        ↓
1 Statement
        ↓

Index Analysis
        ↓
기존 MEMBER_ID Index 확인
        ↓
Composite Index 설계
        ↓
H2 Optimizer 선택 확인
        ↓
PostgreSQL 재검증
```

관련 문서:

```text
docs/database/performance/reservation-n-plus-one.md
```

---

## 18. Final Conclusion

회원별 예약 목록 Query를 대상으로
N+1 최적화 이후 남은 단일 Query의 인덱스 구조와 실행계획을 분석하였다.

대상 Query는 다음과 같다.

```sql
SELECT *
FROM reservations
WHERE member_id = ?
ORDER BY reservation_date DESC,
         reservation_time DESC;
```

Query의 검색 조건과 정렬 조건을 기반으로
다음 복합 인덱스를 설계하였다.

```text
idx_reservation_member_date_time

(member_id, reservation_date, reservation_time)
```

### H2 Analysis

H2에서는 복합 인덱스가 정상적으로 생성되었지만,
Optimizer가 기존 `MEMBER_ID` Foreign Key 인덱스를 계속 선택하였다.

```text
Before
MEMBER_ID FK Index

        ↓ Composite Index 추가

After
MEMBER_ID FK Index
```

따라서 H2 결과만으로는 복합 인덱스의 실제 효과를 확인할 수 없었다.

이 과정에서 테스트 데이터가 한 Member에게 집중되어 있으면
인덱스 선택 실험에 적절하지 않다는 문제도 발견하였다.

초기 데이터 구조:

```text
1 Member × 10,000 Reservations
```

이를 다음과 같이 변경하였다.

```text
10 Members × 1,000 Reservations

Total Reservation  : 10,000
Target Reservation : 1,000
Selectivity         : 10%
```

데이터 분포를 개선하고 `ANALYZE`를 수행했음에도
H2에서는 복합 인덱스가 선택되지 않았다.

따라서 H2의 실행계획을 실제 운영 DBMS의 결과로 일반화하지 않고,
PostgreSQL에서 동일한 조건으로 재검증하였다.

### PostgreSQL Revalidation

PostgreSQL에서는 복합 인덱스를 제거한 Before 상태에서
다음 실행계획이 선택되었다.

```text
Sequential Scan
        ↓
10,000 Rows 확인
        ↓
9,000 Rows 제거
        ↓
1,000 Rows
        ↓
Sort
```

실행 결과:

```text
Scan Type      : Sequential Scan
Rows Removed   : 9,000
Buffers        : shared hit=94
Planning Time  : 0.315 ms
Execution Time : 0.363 ms
```

이후 다음 복합 인덱스를 적용하였다.

```sql
CREATE INDEX idx_reservation_member_date_time
ON reservations (
    member_id,
    reservation_date,
    reservation_time
);
```

`ANALYZE` 수행 후 동일한 Query를 다시 측정한 결과,
PostgreSQL Optimizer가 해당 복합 인덱스를 실제로 선택하였다.

```text
Bitmap Index Scan
        ↓
Bitmap Heap Scan
        ↓
1,000 Rows
        ↓
Sort
```

실행 결과:

```text
Scan Type      : Bitmap Index Scan + Bitmap Heap Scan
Selected Index : idx_reservation_member_date_time
Buffers        : shared hit=10 read=6
Planning Time  : 0.094 ms
Execution Time : 0.184 ms
```

Before / After를 비교하면 다음과 같다.

| Metric | Before | After |
|---|---|---|
| Scan Type | Sequential Scan | Bitmap Index Scan + Bitmap Heap Scan |
| Result Rows | 1,000 | 1,000 |
| Rows Removed by Filter | 9,000 | - |
| Composite Index Used | No | Yes |
| Sort | Quicksort | Quicksort |
| Buffers | 94 hit | 10 hit + 6 read |
| Planning Time | 0.315 ms | 0.094 ms |
| Execution Time | 0.363 ms | 0.184 ms |

해당 실행에서 Execution Time은:

```text
0.363 ms
    ↓
0.184 ms
```

로 약 **49.3% 감소**하였다.

다만 실행시간과 Buffer 사용량은
DB Cache와 시스템 상태에 영향을 받을 수 있으므로,
단일 실행 결과를 일반적인 성능 향상률로 확정하지 않는다.

이번 실험에서 더 중요한 결과는
실제 PostgreSQL 실행계획이:

```text
Sequential Scan

        ↓

Bitmap Index Scan
+
Bitmap Heap Scan
```

으로 변경되었고,
설계한 복합 인덱스가 실제로 사용되었다는 점이다.

또한 복합 인덱스에 정렬 컬럼이 포함되어 있음에도
PostgreSQL은 이번 데이터 분포에서 일반적인 Index Scan이 아닌
Bitmap Scan을 선택하였다.

Bitmap Scan은 인덱스의 정렬 순서를 그대로 유지하지 않으므로
다음 Sort 단계는 실행계획에 남았다.

```text
Sort
→ reservation_date DESC
→ reservation_time DESC
```

이는 인덱스 생성만으로 원하는 실행계획이 항상 선택되는 것이 아니라,
Optimizer가 데이터 분포와 비용을 기반으로 실행계획을 결정한다는 것을 보여준다.

### Final Decision

H2에서는 복합 인덱스의 효과를 확인하지 못했지만,
PostgreSQL에서는 실제 실행계획에서 해당 인덱스가 사용되는 것을 확인하였다.

따라서 GarageCare의 회원별 예약 목록 조회를 위해
다음 복합 인덱스를 유지한다.

```text
idx_reservation_member_date_time
(member_id, reservation_date, reservation_time)
```

이번 실험을 통해 다음을 확인하였다.

- 인덱스 생성과 실제 인덱스 사용은 별개의 문제이다.
- 인덱스 실험에서는 데이터 개수뿐 아니라 데이터 분포도 중요하다.
- Optimizer는 존재하는 인덱스를 반드시 선택하지 않는다.
- H2 실행계획을 실제 운영 DBMS의 결과로 일반화해서는 안 된다.
- 실제 성능 검증에는 `EXPLAIN (ANALYZE, BUFFERS)`와 같은 실행계획 분석이 필요하다.
- 단일 실행시간만으로 성능 향상률을 일반화해서는 안 된다.
- GarageCare의 복합 인덱스는 PostgreSQL에서 실제 조회 계획에 사용된다.

이전 N+1 최적화와 이번 인덱스 최적화를 연결하면
예약 목록 조회는 다음 두 단계에서 개선되었다.

```text
Reservation List
        ↓

N+1 Optimization
        ↓
11 Statements
        ↓
@EntityGraph
        ↓
1 Statement

        ↓

Index Optimization
        ↓
Sequential Scan
        ↓
Composite Index
        ↓
Bitmap Index Scan
+
Bitmap Heap Scan
```

즉 N+1 실험에서는 **애플리케이션과 DB 사이의 접근 횟수**를 줄였고,
이번 인덱스 실험에서는 **DB 내부에서 남은 Query가 처리되는 방식**을 분석하고 개선하였다.

최종적으로 GarageCare 예약 목록 조회에 대해:

```text
Application Level
→ JDBC Statement 11 → 1

Database Level
→ Sequential Scan → Composite Index 기반 Bitmap Scan
```

이라는 두 단계의 최적화와 검증을 완료하였다.

---

## 19. Related

### Parent

```text
docs/database/database-performance.md
```

### Previous Experiment

```text
docs/database/performance/reservation-n-plus-one.md
```

### Next Experiment

```text
PostgreSQL Index Revalidation
```

### Related Source

```text
src/main/java/com/hyunu/garagecare/reservation/
src/test/java/com/hyunu/garagecare/reservation/repository/
```
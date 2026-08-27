# GarageCare Database Performance

> Version: 1.1.0  
> Status: In Progress  
> Last Updated: 2026-08-28

---

## 1. Overview

GarageCare에서 발생할 수 있는 데이터베이스 성능 문제를 직접 재현하고,
측정 결과를 기반으로 개선한다.

단순히 최적화 기능을 적용하는 것이 아니라 다음 과정을 반복한다.

```text
문제 발견
→ 재현
→ 측정
→ 원인 분석
→ 개선
→ 동일 조건 재측정
→ 결과 문서화
```

각 실험은 실제 수치와 실행 결과를 기준으로 기록하며,
확인되지 않은 성능 개선을 추정하지 않는다.

---

## 2. Goals

이번 Database Optimization 단계의 목표는 다음과 같다.

- JPA 조회 과정의 불필요한 Query 제거
- 실제 Query의 Index 사용 여부 확인
- Execution Plan 기반 조회 구조 분석
- 대량 데이터 환경의 Pagination 검증
- 동시 예약 충돌 재현
- Database Constraint를 통한 데이터 무결성 강화
- Lock과 Transaction을 이용한 동시성 제어 검토
- 운영 DBMS에서 동일 실험 재검증

---

## 3. Experiment Flow

GarageCare의 DB 성능 실험은 다음 순서로 진행한다.

```text
Reservation List
        ↓

1. N+1 Analysis
        ↓
DB 접근 횟수 최적화
        ↓

2. Index / Execution Plan
        ↓
개별 Query 실행 방식 분석
        ↓

3. PostgreSQL Revalidation
        ↓
운영 DBMS 기준 재측정
        ↓

4. Pagination
        ↓
대량 데이터 조회 검증
        ↓

5. Concurrent Reservation
        ↓
동시 예약 충돌 재현
        ↓

6. Constraint / Lock
        ↓
데이터 무결성과 동시성 제어 검증
```

---

## 4. Experiments

### 4.1 Reservation List N+1

예약 목록 조회에서 `Reservation → Vehicle` 지연 로딩으로 인해
발생하는 N+1 문제를 재현하고 개선하였다.

```text
Before
11 Statements

    ↓ @EntityGraph

After
1 Statement
```

동일한 테스트 조건에서 JDBC PreparedStatement 측정 횟수가
11회에서 1회로 감소하였다.

**Status:** Completed

Detail:

```text
docs/database/performance/reservation-n-plus-one.md
```

---

### 4.2 Reservation Index & Execution Plan

회원별 예약 목록 Query를 분석하여 다음 복합 인덱스를 검토하였다.

```text
(member_id, reservation_date, reservation_time)
```

H2에서는 복합 인덱스가 정상적으로 생성되었지만,
Optimizer는 기존 `MEMBER_ID` Foreign Key 인덱스를 계속 선택하였다.

```text
Composite Index 생성 성공

≠

Optimizer 선택 확인

≠

성능 개선 확인
```

따라서 H2 결과만으로 성능 개선을 확정하지 않고
PostgreSQL 환경에서 다시 검증한다.

**Status:** H2 Analysis Completed / PostgreSQL Revalidation Planned

Detail:

```text
docs/database/performance/reservation-index.md
```

---

### 4.3 PostgreSQL Revalidation

H2에서 수행한 성능 실험을
실제 운영 환경에 가까운 PostgreSQL에서 다시 수행한다.

주요 검증 항목:

- `EXPLAIN ANALYZE`
- Sequential Scan / Index Scan
- Estimated Rows / Actual Rows
- Planning Time
- Execution Time
- 복합 인덱스 적용 전후 비교

**Status:** Planned

---

### 4.4 Pagination

예약 데이터가 증가했을 때
전체 데이터를 한 번에 조회하지 않고 Page 단위로 처리하도록 개선한다.

검토 대상:

```text
Offset Pagination
vs
Keyset Pagination
```

주요 검증 항목:

- 데이터 규모별 조회 시간
- Offset 증가에 따른 성능 변화
- 정렬 기준
- Index와 Pagination의 관계

**Status:** Planned

---

### 4.5 Concurrent Reservation

동일한 예약 시간에 여러 요청이 동시에 발생하는 상황을 재현한다.

예시:

```text
User A ─┐
        ├── 2026-09-01 14:00 예약
User B ─┘
```

동시에 두 요청이 성공할 경우
예약 중복 문제가 발생할 수 있다.

주요 검증 항목:

- 동시 요청 재현
- Transaction 경계
- Race Condition
- 중복 예약 발생 여부

**Status:** Planned

---

### 4.6 Constraint & Lock

동시 예약 문제를 해결하기 위해
Database Constraint와 Lock 전략을 비교한다.

검토 대상:

```text
Unique Constraint
Optimistic Lock
Pessimistic Lock
```

목표는 단순히 Lock을 적용하는 것이 아니라,
GarageCare의 실제 예약 규모와 요구사항에 적절한 방식을 선택하는 것이다.

**Status:** Planned

---

## 5. Measurement Principles

모든 성능 실험은 다음 원칙을 따른다.

### 동일 조건 비교

Before와 After는 가능한 한 동일한 조건을 유지한다.

```text
동일 데이터
동일 Query
동일 테스트 환경
동일 측정 방식
```

### 실제 결과만 기록

다음과 같은 표현은 사용하지 않는다.

```text
"아마 빨라질 것이다."
"인덱스를 추가했으므로 성능이 개선되었다."
```

실제 측정 또는 실행계획으로 확인된 결과만 기록한다.

### 측정 지표를 정확하게 표현

예를 들어 N+1 실험의:

```text
11 → 1
```

은 서비스 성능 향상률이 아니라
JDBC PreparedStatement 측정 횟수의 변화이다.

각 실험에서 무엇을 측정했는지 명확하게 구분한다.

### 테스트 DB와 운영 DB를 구분

H2 결과를 운영 DBMS 결과로 일반화하지 않는다.

```text
H2
→ 빠른 재현 및 구조 검증

PostgreSQL
→ 운영 환경 기준 실행계획 및 성능 검증
```

---

## 6. Current Progress

| Experiment | Result | Status |
|---|---|---|
| Reservation List N+1 | 11 → 1 Statements | Completed |
| Reservation Index / Execution Plan | H2가 기존 MEMBER_ID Index 선택 | H2 Completed |
| PostgreSQL Revalidation | - | Planned |
| Pagination | - | Planned |
| Concurrent Reservation | - | Planned |
| Constraint / Lock | - | Planned |

---

## 7. Documentation Structure

```text
docs/database/
│
├── database-performance.md
│
└── performance/
    ├── reservation-n-plus-one.md
    └── reservation-index.md
```

`database-performance.md`는 전체 실험의 진행 상황과 방향을 관리한다.

각 실험의 상세 과정, 코드, 측정값 및 분석 결과는
`performance/` 하위 문서에서 관리한다.

향후 실험이 추가되면 다음과 같이 확장한다.

```text
performance/
├── reservation-n-plus-one.md
├── reservation-index.md
├── reservation-pagination.md
├── reservation-concurrency.md
└── reservation-lock.md
```

---

## 8. Next Step

현재 다음 단계는 PostgreSQL 환경 구축과
Reservation Index 실험 재검증이다.

```text
H2 Index Analysis
        ↓
PostgreSQL 연결
        ↓
대량 데이터 생성
        ↓
EXPLAIN ANALYZE
        ↓
Before 측정
        ↓
Composite Index 적용
        ↓
After 재측정
```

이후 Pagination과 동시 예약 문제로 실험 범위를 확장한다.

---

## 9. Related Docs

```text
docs/database/performance/reservation-n-plus-one.md
docs/database/performance/reservation-index.md

docs/domain-model.md
docs/erd.md
docs/architecture/transaction-policy.md
```
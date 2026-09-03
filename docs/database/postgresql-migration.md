# PostgreSQL Development Environment

> Version: 1.1.1  
> Status: In Progress  
> Last Updated: 2026-09-02

---

## 1. Overview

GarageCare의 개발 데이터베이스를 H2에서 **PostgreSQL로 전환**하였다.

H2는 빠른 테스트 환경으로 유지하고,
PostgreSQL에서는 실제 운영 환경에 가까운 스키마, 인덱스,
실행 계획 및 데이터베이스 동작을 검증한다.

---

## 2. Environment

| Item | Version |
| --- | --- |
| PostgreSQL | 17.11 |
| Spring Boot | 4.1.0 |
| Hibernate ORM | 7.4.1.Final |
| Java | 17 |

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/garagecare}
spring.datasource.username=${DB_USERNAME:garagecare}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=create
spring.jpa.open-in-view=false
```

DB 비밀번호는 Repository에 포함하지 않고 환경 변수로 관리한다.

---

## 3. Schema

PostgreSQL에서 다음 핵심 테이블의 생성을 확인하였다.

```text
members
vehicles
maintenance_items
reservations
reservation_items
```

예약 테이블은 다른 테이블과 명명 규칙을 통일하기 위해
`reservation`에서 `reservations`로 변경하였다.

---

## 4. Reservation Index

회원별 최신 예약 조회 패턴을 기준으로 복합 인덱스를 정의하였다.

```sql
SELECT *
FROM reservations
WHERE member_id = ?
ORDER BY reservation_date DESC,
         reservation_time DESC;
```

```java
@Table(
    name = "reservations",
    indexes = {
        @Index(
            name = "idx_reservation_member_date_time",
            columnList = "member_id, reservation_date, reservation_time"
        )
    }
)
```

복합 인덱스:

```text
idx_reservation_member_date_time
(member_id, reservation_date, reservation_time)
```

---

## 5. Migration Issues

PostgreSQL 전환 과정에서 JPA 매핑과 실제 PostgreSQL Schema 사이의
불일치를 확인하고 수정하였다.

### 5.1 Reservation Table Naming

#### Problem

기존 예약 테이블명이 단수형으로 생성되고 있었다.

```text
reservation
```

다른 테이블은 복수형 명명 규칙을 사용하고 있어
스키마 전체의 일관성이 떨어졌다.

#### Fix

```java
@Table(name = "reservations")
```

로 변경하였다.

이 과정에서 `mappedBy` 역시 `reservations`로 변경하면서
다음 오류가 발생하였다.

```text
mappedBy a property named 'reservations'
which does not exist in ReservationItem
```

#### Cause

`mappedBy`는 DB 테이블명이 아니라
연관관계의 주인 Entity에 선언된 **Java 필드명**을 참조한다.

`ReservationItem`의 필드는 다음과 같다.

```java
private Reservation reservation;
```

따라서 다음과 같이 수정하였다.

```java
mappedBy = "reservation"
```

#### Result

DB 테이블명과 Java 연관관계 필드의 역할을 분리하였다.

```text
@Table(name = "reservations")  → DB Table
mappedBy = "reservation"       → Java Field
```

JPA EntityManagerFactory가 정상적으로 초기화되었다.

---

### 5.2 Composite Index Mapping

#### Problem

PostgreSQL Schema 생성 과정에서 복합 인덱스 생성이 실패하였다.

```text
ERROR: column "reservation-date" does not exist
```

생성 시도된 인덱스:

```text
member_id, reservation-date, reservation_item
```

#### Cause

`@Index.columnList`에 실제 PostgreSQL 컬럼명과 일치하지 않는
컬럼명이 지정되어 있었다.

실제 컬럼은 다음과 같다.

```text
member_id
reservation_date
reservation_time
```

#### Fix

```java
@Index(
    name = "idx_reservation_member_date_time",
    columnList = "member_id, reservation_date, reservation_time"
)
```

으로 수정하였다.

#### Result

PostgreSQL에서 다음 복합 인덱스가 정상적으로 생성되도록 수정하였다.

```text
idx_reservation_member_date_time
(member_id, reservation_date, reservation_time)
```

---

## 6. Verification

`PostgreSqlSchemaTest`를 통해 다음 항목을 자동 검증한다.

- PostgreSQL 연결
- GarageCare 핵심 테이블 생성
- `reservations` 테이블 생성
- 예약 조회 복합 인덱스 생성

이를 통해 JPA Mapping과 실제 PostgreSQL Schema가
일치하는지 검증한다.

---

## 7. H2 and PostgreSQL

| Environment | Purpose |
| --- | --- |
| H2 | 빠른 일반 테스트 |
| PostgreSQL | 개발 및 DB 동작/성능 검증 |

H2에서 수행했던 데이터베이스 최적화 실험을
PostgreSQL에서도 다시 수행하여 DBMS에 따른 차이를 확인한다.

---

## 8. PostgreSQL Revalidation Result

PostgreSQL에서 동일한 예약 조회 Query를 대상으로
복합 인덱스 적용 전후의 실행계획을 비교하였다.

테스트 데이터:

```text
Members                  10
Reservations per Member  1,000
Total Reservations       10,000
Target Reservations      1,000
```

측정에는 PostgreSQL의 다음 명령을 사용하였다.

```sql
EXPLAIN (ANALYZE, BUFFERS)
```

### Before

복합 인덱스를 제거한 상태에서는 `Sequential Scan`이 발생하였다.

```text
Seq Scan on reservations
Rows Removed by Filter: 9000

Execution Time: 0.363 ms
Buffers: shared hit=94
```

전체 10,000건을 탐색한 후 조건에 맞지 않는
9,000건을 제거하였다.

### After

다음 복합 인덱스를 적용하였다.

```text
idx_reservation_member_date_time
(member_id, reservation_date, reservation_time)
```

PostgreSQL은 해당 인덱스를 실제 실행계획에 사용하였다.

```text
Bitmap Index Scan
→ Bitmap Heap Scan
→ Sort

Execution Time: 0.184 ms
Buffers: shared hit=10 read=6
```

단일 실행 기준 Execution Time은 다음과 같이 변화하였다.

```text
0.363 ms → 0.184 ms
```

약 49.3% 감소하였다.

단, 실행시간과 Buffer 상태는 캐시 및 실행 환경의 영향을 받을 수 있으므로
이를 일반적인 성능 향상률로 단정하지 않는다.

상세 분석은
`performance/reservation-index.md`에서 관리한다.

---

## 9. Conclusion

PostgreSQL 전환을 통해 다음을 확인하였다.

- JPA Entity와 PostgreSQL Schema의 일치 여부 검증
- 예약 테이블 명명 규칙 정리
- 복합 인덱스 생성 검증
- `EXPLAIN ANALYZE` 기반 실행계획 확인
- PostgreSQL Optimizer의 복합 인덱스 사용 확인

H2에서는 복합 인덱스의 실제 선택을 명확하게 확인하지 못했지만,
PostgreSQL에서는 `idx_reservation_member_date_time`이
실제 실행계획에 사용되는 것을 확인하였다.

따라서 해당 복합 인덱스를 유지한다.

H2는 빠른 일반 테스트 환경으로 유지하고,
PostgreSQL은 실제 DB 동작과 성능 검증을 위한 개발 환경으로 사용한다.
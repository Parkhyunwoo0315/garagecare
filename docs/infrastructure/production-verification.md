# Production Deployment Verification
> Version: 1.0.0  
> Status: Draft  
> Last Updated: 2026-09-16
---

> GarageCare Production Deployment Verification  
> Status: Local Production-like Verification Completed

---

## 1. Overview

GarageCare의 운영 배포 전 애플리케이션 동작을 검증한다.

이번 검증에서는 macOS Docker 환경에서 실제 운영 설정과 동일한
`prod` Spring Profile을 활성화하고 PostgreSQL을 연결하여
애플리케이션의 핵심 사용자 흐름과 데이터 영속성을 확인했다.

검증 범위는 다음과 같다.

```text
Application Build
        ↓
Production Profile
        ↓
PostgreSQL Connection
        ↓
Application Startup
        ↓
Health Check
        ↓
Member / Vehicle Flow
        ↓
Reservation Creation
        ↓
Reservation Cancellation
        ↓
Database Verification
        ↓
Application Restart
        ↓
Persistence Verification
```

이번 검증은 AWS EC2/RDS 환경 자체에 대한 최종 검증이 아니라,
로컬 Docker 환경에서 `prod` Profile을 사용하는
Production-like 검증이다.

AWS 운영 환경 검증은 별도로 수행한다.

---

## 2. Verification Environment

### Application

```text
Spring Boot
Java 17
Spring Profile: prod
```

### Runtime

```text
Docker
Docker Compose
garagecare:prod
```

### Database

```text
PostgreSQL 17
Docker Container: garagecare-db
Database: garagecare
```

애플리케이션 컨테이너에서는 Docker Host의 PostgreSQL에 접근하도록
다음 JDBC URL을 사용했다.

```text
jdbc:postgresql://host.docker.internal:5433/garagecare
```

---

## 3. Production Profile Verification

컨테이너 환경 변수를 확인했다.

```bash
docker exec garagecare-app printenv | \
grep -E 'SPRING_PROFILES_ACTIVE|DB_URL|DB_USERNAME'
```

확인 결과:

```text
DB_URL=jdbc:postgresql://host.docker.internal:5433/garagecare
DB_USERNAME=garagecare
SPRING_PROFILES_ACTIVE=prod
```

애플리케이션 로그에서도 다음 내용을 확인했다.

```text
The following 1 profile is active: "prod"

Database JDBC URL
jdbc:postgresql://host.docker.internal:5433/garagecare

Database driver
PostgreSQL JDBC Driver

Database dialect
PostgreSQLDialect

Database version
17.11
```

결과:

```text
Production Profile      PASS
PostgreSQL Connection   PASS
```

---

## 4. Application Health Verification

Docker Compose 상태를 확인했다.

```bash
docker compose -f compose.prod.yaml ps
```

애플리케이션 컨테이너가 다음 상태로 실행되는 것을 확인했다.

```text
garagecare-app
Up
healthy
```

Actuator Health Endpoint도 확인했다.

```bash
curl -f http://localhost:8080/actuator/health
```

결과:

```json
{
  "groups": [
    "liveness",
    "readiness"
  ],
  "status": "UP"
}
```

결과:

```text
Application Startup     PASS
Docker Health Check     PASS
Actuator Health Check   PASS
```

---

## 5. Reservation Flow Verification

브라우저를 이용하여 실제 사용자 흐름을 검증했다.

### Vehicle Selection

로그인한 회원에게 등록된 차량이 예약 화면에 표시되는 것을 확인했다.

```text
Vehicle
E225 (268가8986)
```

결과:

```text
Member Vehicle Lookup   PASS
Vehicle Selection       PASS
```

### Maintenance Item Selection

예약 화면에서 활성화된 정비 항목이 정상적으로 조회되는 것을 확인했다.

검증 당시 다음 항목들이 표시되었다.

```text
엔진오일 교환
브레이크 점검
타이어 점검
배터리 점검
냉각수 점검
```

복수 정비 항목 선택도 정상적으로 동작했다.

검증 예약에서는 다음 항목을 선택했다.

```text
배터리 점검   20,000원
냉각수 점검   30,000원
```

결과:

```text
Maintenance Item Lookup     PASS
Multiple Item Selection     PASS
```

---

## 6. Reservation Creation Verification

다음 조건으로 예약을 생성했다.

```text
Vehicle
268가8986

Reservation Date
2026-09-16

Reservation Time
20:02

Maintenance Items
- 배터리 점검
- 냉각수 점검
```

예약 생성 후 상세 화면으로 정상 이동했으며,
예약 번호 `160002`가 생성되었다.

상세 화면에서 다음 내용을 확인했다.

```text
Reservation ID     160002
Vehicle            268가8986
Date               2026-09-16
Time               20:02
Status             PENDING

Maintenance Items
- 배터리 점검   20,000원
- 냉각수 점검   30,000원
```

예약 목록에도 생성된 예약이 정상적으로 표시되었다.

결과:

```text
Reservation Creation       PASS
Reservation Detail         PASS
Reservation List           PASS
Reservation Item Mapping   PASS
```

---

## 7. Reservation Cancellation Verification

예약 상세 화면에서 예약 취소 기능을 실행했다.

취소 요청 전 사용자에게 다음 확인 절차가 표시되는 것을 확인했다.

```text
예약을 취소하시겠습니까?
```

취소 확인 후 예약 상태가 다음과 같이 변경되었다.

```text
PENDING
   ↓
CANCELED
```

예약 상세 화면과 예약 목록 모두에서
`CANCELED` 상태가 정상적으로 반영되었다.

결과:

```text
Reservation Cancellation   PASS
Status Transition          PASS
Reservation List Update    PASS
```

---

## 8. Database Verification

브라우저 검증 이후 PostgreSQL에서 실제 데이터를 확인했다.

### Reservations

```sql
SELECT
    id,
    member_id,
    vehicle_id,
    reservation_date,
    reservation_time,
    status
FROM reservations
ORDER BY id DESC
LIMIT 5;
```

결과:

```text
160002 | 90 | 89 | 2026-09-16 | 20:02:00 | CANCELED
160001 | 90 | 89 | 2026-09-16 | 20:01:00 | CANCELED
```

애플리케이션의 예약 취소 결과가 DB에도 정상적으로 반영된 것을 확인했다.

### Reservation Items

예약과 정비 항목의 관계를 확인했다.

```sql
SELECT
    ri.id,
    ri.reservation_id,
    ri.maintenance_item_id,
    mi.name,
    mi.estimated_price
FROM reservation_items ri
JOIN maintenance_items mi
    ON mi.id = ri.maintenance_item_id
ORDER BY ri.id DESC
LIMIT 10;
```

결과:

```text
4 | 160002 | 5 | 냉각수 점검   | 30000
3 | 160002 | 4 | 배터리 점검   | 20000
2 | 160001 | 3 | 타이어 점검   | 30000
1 | 160001 | 2 | 브레이크 점검 | 50000
```

예약 `160002`에 선택했던 두 정비 항목이 정상적으로 연결되어 있음을 확인했다.

결과:

```text
Reservation DB Write        PASS
ReservationItem DB Write    PASS
Cancellation DB Update      PASS
```

---

## 9. Database State

검증 완료 시점의 데이터 개수를 확인했다.

```sql
SELECT
    (SELECT COUNT(*) FROM members) AS members,
    (SELECT COUNT(*) FROM vehicles) AS vehicles,
    (SELECT COUNT(*) FROM maintenance_items) AS maintenance_items,
    (SELECT COUNT(*) FROM reservations) AS reservations,
    (SELECT COUNT(*) FROM reservation_items) AS reservation_items;
```

결과:

```text
members              2
vehicles             1
maintenance_items    5
reservations         2
reservation_items    4
```

검증에 사용된 회원, 차량, 정비 항목, 예약 및 예약 정비 항목 데이터가
PostgreSQL에 정상적으로 저장되어 있음을 확인했다.

---

## 10. Restart Persistence Verification

애플리케이션 컨테이너를 재시작하여
애플리케이션 재시작 이후 데이터 영속성을 검증했다.

```bash
docker compose -f compose.prod.yaml restart app
```

재시작 후 예약 목록에서 기존 예약이 그대로 조회되었다.

```text
160002   CANCELED
160001   CANCELED
```

예약 `160002`의 상세 화면에서도 다음 데이터가 유지되었다.

```text
Vehicle
268가8986

Status
CANCELED

Maintenance Items
- 배터리 점검   20,000원
- 냉각수 점검   30,000원
```

DB에서도 다시 확인했다.

```bash
docker exec garagecare-db \
  psql -U garagecare -d garagecare \
  -c "SELECT id, vehicle_id, status FROM reservations ORDER BY id DESC LIMIT 5;"
```

결과:

```text
160002 | 89 | CANCELED
160001 | 89 | CANCELED
```

애플리케이션 컨테이너 재시작 이후에도
PostgreSQL 데이터가 정상적으로 유지되는 것을 확인했다.

결과:

```text
Application Restart      PASS
Reservation Persistence  PASS
Database Persistence     PASS
```

---

## 11. Verification Result

| Verification | Result |
|---|---|
| Production Profile | PASS |
| PostgreSQL Connection | PASS |
| Application Startup | PASS |
| Docker Health Check | PASS |
| Actuator Health Check | PASS |
| Member Vehicle Lookup | PASS |
| Vehicle Selection | PASS |
| Maintenance Item Lookup | PASS |
| Multiple Maintenance Item Selection | PASS |
| Reservation Creation | PASS |
| Reservation Detail | PASS |
| Reservation List | PASS |
| ReservationItem Mapping | PASS |
| Reservation Cancellation | PASS |
| PENDING → CANCELED | PASS |
| Reservation DB Write | PASS |
| ReservationItem DB Write | PASS |
| Cancellation DB Update | PASS |
| Application Restart | PASS |
| Reservation Persistence | PASS |
| Database Persistence | PASS |

---

## 12. Incident Found During Verification

초기 Production 검증 과정에서 차량이 등록되지 않은 회원이
예약을 생성할 경우 HTTP 500이 발생하는 문제가 발견되었다.

원인은 차량 선택값이 없는 상태에서 `vehicleId = null`이
Service Layer까지 전달되고 Repository 조회가 실행되는 흐름이었다.

```text
No Vehicle
    ↓
vehicleId = null
    ↓
ReservationService
    ↓
findById(null)
    ↓
IllegalArgumentException
    ↓
HTTP 500
```

또한 Controller에서 Validation Error를 확인한 뒤에도
Service 호출을 계속 수행하는 문제가 있었다.

이를 수정하여 Validation Error 발생 시 예약 Form으로 반환하도록 하고,
Service Layer에서도 null Vehicle ID를 방어하도록 보완했다.

차량 등록 및 조회 흐름도 추가하여 로그인 회원이 자신의 차량을 등록하고
예약 시 해당 차량을 선택할 수 있도록 구성했다.

수정 후 전체 예약 흐름을 다시 검증했으며 정상 동작을 확인했다.

```text
Login
  ↓
Vehicle Registration / Lookup
  ↓
Reservation Form
  ↓
Vehicle Selection
  ↓
Maintenance Item Selection
  ↓
Reservation Creation
  ↓
Reservation Detail
  ↓
Reservation Cancellation
  ↓
Database Verification

PASS
```

---

## 13. Conclusion

GarageCare의 핵심 예약 흐름을
Production Profile 기반 Docker 환경에서 검증했다.

이번 검증을 통해 다음 흐름을 확인했다.

```text
Production Configuration
        ↓
PostgreSQL Connection
        ↓
Health Check
        ↓
Vehicle Integration
        ↓
Reservation Creation
        ↓
Reservation Item Persistence
        ↓
Reservation Cancellation
        ↓
Database State Update
        ↓
Restart Persistence
```

핵심 사용자 흐름과 PostgreSQL 데이터 영속성이
정상적으로 동작하는 것을 확인했다.

이번 결과는 로컬 Docker 기반 Production-like 환경에 대한 검증이며,
실제 AWS 운영 환경에 대한 최종 검증과는 구분한다.

다음 단계에서는 수정 사항을 CI를 통해 재검증한 뒤
AWS EC2/RDS 환경에 배포하여 동일한 Smoke Test를 수행한다.
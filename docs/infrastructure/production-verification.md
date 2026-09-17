# Production Deployment Verification

> Version: 2.0.0  
> Status: Completed  
> Last Updated: 2026-09-18

---

> GarageCare Production Deployment Verification  
> Environment: AWS EC2 / Amazon RDS PostgreSQL  
> Result: PASS

---

## 1. Overview

GarageCare의 1차 Production 배포 환경을 실제 AWS 환경에서 최종 검증한다.

GitHub의 `main` Branch를 기준으로 Docker Image를 빌드하고,
AWS EC2에서 Production Profile로 애플리케이션을 실행하여
Amazon RDS PostgreSQL과 연동되는 전체 운영 흐름을 검증했다.

검증 범위는 다음과 같다.

```text
GitHub main
      ↓
Docker Image Build
      ↓
AWS EC2
      ↓
Spring Profile: prod
      ↓
Amazon RDS PostgreSQL
      ↓
Application Health Check
      ↓
Member / Login
      ↓
Vehicle
      ↓
Reservation
      ↓
ReservationItem
      ↓
Cancellation
      ↓
Database Verification
      ↓
Production Regression Test
      ↓
Final Health / Log Verification
```

검증 과정에서 실제 사용자 흐름을 통해 운영 환경에서만 확인할 수 있었던
문제를 발견했으며, 수정 후 Production에 다시 배포하여 회귀 검증까지 수행했다.

---

## 2. Verification Environment

### Application

```text
Spring Boot 4.1.0
Java 17
Spring Profile: prod
```

### Runtime

```text
AWS EC2
Amazon Linux 2023
Docker
Docker Compose

Image
garagecare:prod

Container
garagecare-app
```

애플리케이션은 EC2의 Docker Container에서 실행되며
EC2의 `8080` Port를 통해 서비스된다.

검증 시 SSH Local Port Forwarding을 사용하여
외부에 Application Port를 직접 노출하지 않고 브라우저 테스트를 수행했다.

### Database

```text
Amazon RDS
PostgreSQL
Database: garagecare
TLS Connection
```

Production Application은 환경 변수로 전달되는 JDBC 설정을 통해
Amazon RDS PostgreSQL에 연결된다.

Database Password 등 Secret 값은 문서에 기록하지 않는다.

---

## 3. Production Profile Verification

Production Container가 `prod` Spring Profile로 실행되는 것을 확인했다.

Production 설정에서는 Database 접속 정보를 환경 변수로 주입하며,
필수 환경 변수가 존재하지 않을 경우 애플리케이션이 정상 실행되지 않도록 구성되어 있다.

또한 Production 환경에서는 Hibernate가 Database Schema를 자동 변경하지 않도록
다음 정책을 사용한다.

```properties
spring.jpa.hibernate.ddl-auto=validate
```

이를 통해 Application Entity와 운영 Database Schema의 일치 여부만 검증하고,
Application Startup 과정에서 운영 Schema를 임의로 생성하거나 수정하지 않는다.

결과:

```text
Production Profile        PASS
Environment Configuration PASS
RDS Connection            PASS
Schema Validation         PASS
```

---

## 4. Application Health Verification

Production Container 상태를 확인했다.

```bash
docker compose -f compose.prod.yaml ps
```

최종 확인 결과:

```text
garagecare-app
Up
healthy
```

Actuator Health Endpoint를 확인했다.

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

HTTP 상태도 별도로 확인했다.

```bash
curl -i http://localhost:8080/actuator/health
```

결과:

```text
HTTP/1.1 200
status = UP
```

결과:

```text
Application Startup     PASS
Docker Health Check     PASS
Actuator Health Check   PASS
HTTP Health Check       PASS
```

---

## 5. Member / Vehicle Flow Verification

실제 Production 환경에서 회원 및 차량 흐름을 검증했다.

로그인하지 않은 사용자가 보호된 URL에 접근할 경우
로그인 화면으로 이동하는 것을 확인했다.

회원가입 후 로그인하여 Session이 생성되고,
로그인 완료 후 차량 화면으로 정상 이동하는 것도 확인했다.

Production 검증에 사용된 차량은 다음과 같다.

```text
Vehicle Number
22나2222

Manufacturer
Honda

Model
NSX Type R (NA1)

Model Year
1992
```

등록된 차량은 예약 Form의 차량 선택 항목에도 정상적으로 표시되었다.

결과:

```text
Signup                  PASS
Login                   PASS
Session                 PASS
Login Redirect          PASS
Vehicle Registration    PASS
Vehicle Lookup          PASS
Reservation Integration PASS
```

---

## 6. Reservation Flow Verification

Production 예약 Form에서 실제 예약 흐름을 검증했다.

활성화된 정비 항목이 다음과 같이 표시되는 것을 확인했다.

```text
엔진오일 교환   100,000원
브레이크 점검    50,000원
타이어 점검      30,000원
배터리 점검      20,000원
냉각수 점검      30,000원
```

예약 생성 전 차량 미선택 Validation도 검증했다.

차량을 선택하지 않은 상태에서 예약을 요청하면
Service Layer로 잘못된 요청이 전달되지 않고
예약 Form이 다시 표시되었다.

```text
차량을 선택해 주세요.
```

이 과정에서 선택했던 정비 항목도 유지되었으며
HTTP 500 오류는 발생하지 않았다.

결과:

```text
Maintenance Item Lookup      PASS
Vehicle Validation           PASS
Validation Form Rendering    PASS
Selected Item Preservation   PASS
```

---

## 7. Reservation Creation Verification

다음 조건으로 실제 Production 예약을 생성했다.

```text
Vehicle
22나2222

Manufacturer / Model
Honda NSX Type R (NA1)

Reservation Date
2026-09-18

Reservation Time
01:10

Maintenance Item
브레이크 점검

Estimated Price
50,000원
```

예약 생성 후 Reservation Detail 화면으로 정상 이동했다.

생성된 Production 검증 예약:

```text
Reservation ID
1

Status
PENDING
```

상세 화면에서 차량, 예약 일자, 예약 시간,
정비 항목 및 예상 가격이 정상적으로 표시되었다.

결과:

```text
Reservation Creation       PASS
Reservation Detail         PASS
Vehicle Mapping            PASS
ReservationItem Mapping    PASS
```

---

## 8. Reservation Cancellation Verification

생성한 Production 예약을 취소하여 상태 변경을 검증했다.

```text
PENDING
   ↓
CANCELED
```

취소 이후 Reservation Detail 화면에서도
`CANCELED` 상태가 정상적으로 표시되었다.

Database에서도 동일한 상태 변경을 확인했다.

```text
Reservation ID   1
Status           CANCELED
```

예약을 취소해도 기존 `reservation_items` 관계는 유지되었다.

결과:

```text
Reservation Cancellation   PASS
Status Transition          PASS
Database Update            PASS
ReservationItem Retention  PASS
```

---

## 9. Database Verification

Amazon RDS PostgreSQL에서 실제 Production 데이터를 직접 확인했다.

### Reservations

예약과 차량 관계를 확인했다.

```sql
SELECT
    r.id,
    r.member_id,
    r.vehicle_id,
    r.reservation_date,
    r.reservation_time,
    r.status,
    v.vehicle_number,
    v.manufacturer,
    v.model
FROM reservations r
JOIN vehicles v
    ON v.id = r.vehicle_id
ORDER BY r.id DESC;
```

Production Application에서 생성한 예약이
Amazon RDS에 정상적으로 저장되어 있음을 확인했다.

```text
Reservation ID   1
Vehicle ID       1
Vehicle Number   22나2222
Manufacturer     Honda
Model            NSX Type R (NA1)
Status           CANCELED
```

### Reservation Items

예약과 정비 항목 관계도 확인했다.

```sql
SELECT
    ri.reservation_id,
    mi.name,
    mi.estimated_price
FROM reservation_items ri
JOIN maintenance_items mi
    ON mi.id = ri.maintenance_item_id
ORDER BY ri.reservation_id;
```

검증 결과:

```text
Reservation ID   1
Maintenance      브레이크 점검
Estimated Price  50000
```

결과:

```text
RDS Write                   PASS
Reservation Persistence     PASS
Vehicle Relation            PASS
ReservationItem Relation    PASS
Cancellation Persistence    PASS
```

---

## 10. Production Data Initialization Verification

Production 환경에서 `maintenance_items`가 초기에는 존재하지 않는 것을 확인했다.

원인을 분석한 결과 개발 환경에서 사용하는
`MaintenanceItemDataInitializer`가 다음 Profile로 제한되어 있었다.

```java
@Profile("dev")
```

따라서 Production Profile에서는 초기 정비 데이터가 자동 생성되지 않는 것이
현재 구현 기준으로 정상적인 동작이었다.

```text
dev
 ↓
MaintenanceItemDataInitializer 실행

prod
 ↓
Initializer 실행 안 함
 ↓
maintenance_items = 0
```

이번 Production 검증에서는 필요한 정비 항목을
운영 Database에 명시적으로 등록한 뒤 예약 흐름을 검증했다.

```text
엔진오일 교환
브레이크 점검
타이어 점검
배터리 점검
냉각수 점검
```

Production에서 개발용 Initializer를 자동 실행하도록 변경하지 않았다.

운영 기준 데이터 초기화는 향후 명시적인 Database Migration 또는
Production Seed 전략으로 관리하는 것이 필요하다.

결과:

```text
Root Cause Identification    PASS
Production Data Preparation  PASS
Reservation Reverification   PASS
```

---

## 11. Production Regression Verification

Production 검증 과정에서 발견된 문제를 수정한 뒤
최신 `main`을 다시 EC2에 배포하여 회귀 검증했다.

최종 Production 배포 기준 Commit:

```text
3fc20df
🔧 Member | 로그인 실패 오류 메시지 렌더링 수정 (#46)
```

Docker Image를 최신 Source 기준으로 다시 빌드했다.

```bash
docker compose -f compose.prod.yaml build app
docker compose -f compose.prod.yaml up -d app
```

재배포 후 Container 상태:

```text
garagecare-app
Up
healthy
```

Actuator:

```text
HTTP 200
status = UP
```

결과:

```text
Latest Main Deployment      PASS
Docker Rebuild              PASS
Container Replacement       PASS
Post-deploy Health Check    PASS
```

---

## 12. Incidents Found During Verification

### 12.1 Vehicle ID Null Reservation Error

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
Exception
    ↓
HTTP 500
```

Controller에서도 Validation Error를 확인한 뒤
Service 호출을 계속 수행하는 문제가 있었다.

이를 다음과 같이 수정했다.

```text
Validation Error
      ↓
Reservation Form Return
      ↓
Service 호출 차단
```

Service Layer에도 null Vehicle ID에 대한 방어 로직을 추가했다.

또한 차량 등록 및 조회 흐름을 구성하여
회원이 자신의 차량을 등록하고 예약 시 선택할 수 있도록 보완했다.

관련 수정:

```text
#45
Reservation | 차량 미등록 상태 예약 오류 및 차량 연동 개선
```

수정 후 Production에서 차량 미선택 요청을 다시 검증했다.

```text
Vehicle Unselected
      ↓
Validation Error
      ↓
"차량을 선택해 주세요."
      ↓
Reservation Form
      ↓
HTTP 500 없음
```

결과:

```text
Root Cause Identified       PASS
Application Fix             PASS
Regression Test             PASS
Production Reverification   PASS
```

### 12.2 Production Maintenance Seed

실제 Amazon RDS에서 다음 Query를 실행했을 때
초기 `maintenance_items`가 존재하지 않는 것을 확인했다.

```sql
SELECT *
FROM maintenance_items
ORDER BY id;
```

원인은 개발용 Initializer가 `@Profile("dev")`로 제한되어 있기 때문이었다.

이는 Production에서 개발용 Seed가 자동 실행되지 않는다는 점에서는
의도된 Profile 분리이지만,
실제 운영을 위해서는 별도의 기준 데이터 초기화 전략이 필요함을 확인했다.

이번 검증에서는 Production Database에 정비 기준 데이터를 명시적으로 등록하여
예약 흐름을 계속 검증했다.

향후 Production 기준 데이터는
Database Migration 기반으로 관리하는 방안을 검토한다.

### 12.3 Login Global Error Rendering

Production 검증 중 잘못된 로그인 정보로 로그인할 경우
로그인 실패 메시지를 렌더링하는 과정에서 HTTP 500 오류가 발견되었다.

Production Log:

```text
Exception evaluating SpringEL expression: "error.defaultMessage"

Property or field 'defaultMessage'
cannot be found on object of type 'java.lang.String'
```

기존 Template:

```html
<p th:each="error : ${#fields.globalErrors()}"
   th:text="${error.defaultMessage}">
```

현재 환경에서 `#fields.globalErrors()`의 오류가 문자열로 렌더링되면서
`defaultMessage` 프로퍼티에 접근할 수 없어
Thymeleaf Template 처리 예외가 발생했다.

다음과 같이 수정했다.

```diff
- th:text="${error.defaultMessage}"
+ th:text="${error}"
```

관련 수정:

```text
#46
Member | 로그인 실패 오류 메시지 렌더링 수정
```

동일 문제의 재발을 방지하기 위해
`MemberControllerTest`에 로그인 실패 회귀 테스트도 추가했다.

로컬 검증:

```text
MemberControllerTest   PASS
Full Test Suite        PASS
```

수정 사항을 Production에 다시 배포한 뒤
실제로 잘못된 비밀번호를 입력하여 회귀 검증했다.

브라우저에서 다음 메시지가 정상적으로 표시되었다.

```text
이메일 또는 비밀번호가 올바르지 않습니다.
```

동시에 다음 사항을 확인했다.

```text
Login Form Rendering       PASS
Error Message Rendering    PASS
HTTP 500 없음              PASS
ERROR / Exception Log 없음 PASS
Actuator Status UP         PASS
Docker Container Healthy   PASS
```

---

## 13. Final Verification Result

| Verification | Result |
|---|---|
| Production Profile | PASS |
| AWS EC2 Deployment | PASS |
| Amazon RDS PostgreSQL Connection | PASS |
| Production Schema Validation | PASS |
| Docker Image Build | PASS |
| Docker Container Health | PASS |
| Actuator Health Check | PASS |
| Signup | PASS |
| Login | PASS |
| Session | PASS |
| Login Redirect | PASS |
| Vehicle Registration / Lookup | PASS |
| Vehicle Selection | PASS |
| Maintenance Item Lookup | PASS |
| Vehicle Validation | PASS |
| Reservation Creation | PASS |
| Reservation Detail | PASS |
| ReservationItem Mapping | PASS |
| Reservation Cancellation | PASS |
| PENDING → CANCELED | PASS |
| RDS Reservation Write | PASS |
| RDS ReservationItem Write | PASS |
| RDS Cancellation Update | PASS |
| Vehicle Null Regression | PASS |
| Login Error Regression | PASS |
| Final Application Log Check | PASS |
| Final Actuator Health | PASS |
| Final Container Health | PASS |

---

## 14. Final Health / Log Verification

최종 수정 사항을 Production에 배포한 이후
Application Log에서 오류 발생 여부를 확인했다.

```bash
docker compose -f compose.prod.yaml logs \
  --since=5m app \
  | grep -Ei "ERROR|Exception"
```

결과:

```text
No ERROR / Exception
```

Actuator Health:

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

Container:

```text
garagecare-app
Up
healthy
```

최종 결과:

```text
Application Error Log   PASS
Actuator Health         PASS
Docker Health           PASS
```

---

## 15. Conclusion

GarageCare의 1차 Production 배포 환경을
실제 AWS EC2와 Amazon RDS PostgreSQL 환경에서 검증했다.

최종 운영 흐름은 다음과 같다.

```text
GitHub main
      ↓
Docker Build
      ↓
AWS EC2
      ↓
Spring Profile: prod
      ↓
Amazon RDS PostgreSQL
      ↓
Member / Session
      ↓
Vehicle
      ↓
Reservation
      ↓
ReservationItem
      ↓
Cancellation
      ↓
RDS Persistence
      ↓
Health / Log Verification
```

검증 과정에서 다음 운영 이슈를 발견했다.

```text
Vehicle ID Null
      ↓
Reservation HTTP 500
      ↓
#45 Fix
      ↓
Production Regression PASS

Production Maintenance Seed 없음
      ↓
Profile 원인 확인
      ↓
Production 기준 데이터 명시적 구성
      ↓
Reservation Flow PASS

Login Global Error Rendering
      ↓
Thymeleaf HTTP 500
      ↓
#46 Fix
      ↓
Production Regression PASS
```

단순히 애플리케이션이 실행되는지만 확인한 것이 아니라,
실제 Production 사용자 흐름과 Database 상태를 기준으로 문제를 발견하고
수정 사항을 다시 Production에 배포하여 회귀 검증까지 완료했다.

최종 상태:

```text
Application     UP
Docker          healthy
Amazon RDS      Connected
Core Flow       PASS
Regression      PASS
ERROR Log       None
```

따라서 GarageCare의 1차 Production 배포 및 핵심 Smoke Test를
완료된 상태로 판단한다.

향후 운영 단계에서는 Production 기준 데이터의 Migration 관리,
배포 자동화 고도화, Monitoring 및 운영 관측성 확장을 진행한다.
# Production Retrospective

> Sprint: Sprint 4 - Deployment & Infrastructure  
> Status: Completed  
> Last Updated: 2026-09-18

---

## 1. Overview

Sprint 4의 목표는 GarageCare를 로컬 개발 환경에서 벗어나
실제 배포 및 운영 가능한 서비스로 전환하는 것이었다.

Sprint 시작 전 GarageCare는 Spring Boot 기반의 핵심 도메인과
예약 기능을 구현하고 있었지만,
실제 Production 환경에서 실행하고 운영해 본 경험은 없는 상태였다.

Sprint 4에서는 다음 흐름을 단계적으로 구축했다.

```text
Local Application
        ↓
Docker
        ↓
PostgreSQL
        ↓
GitHub Actions CI
        ↓
Production Configuration
        ↓
Secret Management
        ↓
Amazon RDS PostgreSQL
        ↓
AWS EC2
        ↓
Health Check / Logging
        ↓
Production Verification
```

최종적으로 단순히 애플리케이션을 AWS에서 실행하는 것에 그치지 않고,
실제 Production 환경에서 사용자 흐름을 수행하며
Application과 Database의 동작을 검증했다.

이 과정에서 로컬 테스트와 단순 Health Check만으로는 발견하지 못했던
문제들이 실제 사용자 흐름에서 확인되었다.

```text
Production Deployment
        ↓
Functional Verification
        ↓
Incident Detection
        ↓
Root Cause Analysis
        ↓
Fix
        ↓
Regression Test
        ↓
Production Redeployment
        ↓
Production Reverification
```

Sprint 4의 가장 중요한 결과는
"배포에 성공했다"는 사실 자체보다
Production에서 발생한 문제를 발견하고 수정한 뒤
다시 Production에서 검증하는 전체 사이클을 경험한 것이다.

---

## 2. Production Architecture

Sprint 4 종료 시점의 GarageCare Production 구조는 다음과 같다.

```text
Developer
    │
    ▼
GitHub
    │
    ▼
GitHub Actions CI
    │
    ▼
AWS EC2
    │
    ▼
Docker
    │
    ▼
GarageCare
    │
    ├── Spring Profile: prod
    ├── Environment Variables
    ├── Actuator Health Check
    └── Application Logging
    │
    ▼
Amazon RDS PostgreSQL
```

각 구성 요소는 다음 역할을 담당한다.

```text
GitHub Actions
→ Build / Test 자동 검증

Docker
→ 실행 환경 표준화

AWS EC2
→ Production Application Runtime

Spring Profile
→ 개발 / 운영 설정 분리

Environment Variables
→ Production Secret 관리

Amazon RDS PostgreSQL
→ Production Database

Actuator
→ Application Health 확인

Application Logs
→ 장애 분석 및 운영 상태 확인
```

Production Database는 Application과 분리했으며,
EC2에서 Amazon RDS PostgreSQL로 연결하는 구조를 사용한다.

Production에서는 Hibernate가 Schema를 임의로 변경하지 않도록
다음 정책을 적용했다.

```properties
spring.jpa.hibernate.ddl-auto=validate
```

이를 통해 Production Application은
기존 Database Schema와 Entity의 일치 여부를 검증하지만
Schema를 자동 생성하거나 변경하지 않는다.

---

## 3. Deployment Journey

Sprint 4에서는 처음부터 AWS 배포를 진행하지 않고,
실행 환경을 단계적으로 Production에 가깝게 전환했다.

```text
Local Spring Boot
        ↓
Docker Application
        ↓
Docker + PostgreSQL
        ↓
GitHub Actions CI
        ↓
Production Profile
        ↓
Production Secret
        ↓
Amazon RDS
        ↓
AWS EC2
        ↓
Production Verification
```

이 접근을 통해 각 단계에서 발생하는 문제의 범위를
상대적으로 명확하게 분리할 수 있었다.

예를 들어 Database 문제가 발생했을 때

```text
Application
Docker
Network
Database
Production Configuration
```

중 어느 영역에서 발생한 문제인지 단계적으로 확인할 수 있었다.

또한 개발 환경과 Production 환경의 차이를
Spring Profile과 환경 변수로 명확하게 분리했다.

---

## 4. Production Verification

AWS EC2와 Amazon RDS 배포가 완료된 이후
단순한 Application Startup 확인에서 검증을 종료하지 않았다.

먼저 Infrastructure 수준에서 다음 상태를 확인했다.

```text
Application Startup     PASS
Docker Container        healthy
Actuator                UP
Amazon RDS Connection   PASS
Production Profile      PASS
```

그다음 실제 사용자가 서비스를 사용하는 흐름을 기준으로 검증했다.

```text
Signup
  ↓
Login
  ↓
Session
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
Amazon RDS Verification
```

이 단계가 Sprint 4에서 중요한 전환점이었다.

Infrastructure Health Check만 확인했다면
GarageCare는 이미 정상적인 Production 서비스처럼 보였다.

하지만 실제 사용자 흐름을 실행하면서
Application-level 문제가 발견되기 시작했다.

---

## 5. Incident #1 - Reservation Vehicle Validation

### Problem

Production Functional Verification 과정에서
차량이 등록되지 않았거나 차량을 선택하지 않은 상태에서
예약을 생성하면 HTTP 500이 발생하는 문제가 확인되었다.

흐름은 다음과 같았다.

```text
Reservation Request
        ↓
No Vehicle
        ↓
vehicleId = null
        ↓
Validation Error
        ↓
Service 호출 계속 진행
        ↓
findById(null)
        ↓
Exception
        ↓
HTTP 500
```

### Root Cause

문제는 하나의 Layer에만 존재하지 않았다.

Controller에서는 Validation Error가 발생했지만
예약 Form으로 즉시 반환하지 않고
Service 호출을 계속 수행하고 있었다.

또한 Service Layer에서도 `vehicleId = null`에 대한
방어가 존재하지 않았다.

즉,

```text
Controller Validation Control Flow
+
Service Defensive Validation
```

두 영역 모두 보완이 필요했다.

### Fix

관련 수정은 다음 Fix에서 진행했다.

```text
#45
Reservation | 차량 미등록 상태 예약 오류 및 차량 연동 개선
```

주요 수정 사항:

```text
Controller
├── Validation Error 발생 시 Form 즉시 반환
└── 잘못된 Service 호출 방지

Service
└── null Vehicle ID 방어

Vehicle
├── 회원 차량 등록
├── 회원 차량 조회
└── Reservation Form 연동

Member
└── Login 이후 Redirect Flow 보완

Test
└── Regression Test 추가
```

수정 후 전체 테스트를 통과시킨 뒤
Production 환경에서 동일한 상황을 다시 검증했다.

```text
No Vehicle Selection
        ↓
Validation
        ↓
"차량을 선택해 주세요."
        ↓
Reservation Form
        ↓
HTTP 500 없음
```

### Lesson

이 문제를 통해 Bean Validation이 존재한다는 것과
잘못된 요청이 실제 Business Logic까지 도달하지 않는다는 것은
서로 다른 문제라는 점을 확인했다.

Validation Error를 탐지하는 것뿐만 아니라
그 결과에 따라 Control Flow가 정확하게 종료되어야 한다.

또한 Service Layer도 Controller가 항상 올바른 값을 전달한다고
가정해서는 안 된다는 점을 확인했다.

---

## 6. Incident #2 - Production Reference Data

### Problem

#45 수정 이후 Production Reservation Flow를 다시 검증하는 과정에서
예약 화면에 정비 항목이 존재하지 않는 문제가 확인되었다.

Amazon RDS를 직접 조회한 결과:

```text
maintenance_items = 0
```

이었다.

### Root Cause

GarageCare에는 개발 편의를 위한
`MaintenanceItemDataInitializer`가 존재했다.

하지만 해당 Initializer에는 다음 Profile이 적용되어 있었다.

```java
@Profile("dev")
```

따라서 실행 흐름은 다음과 같았다.

```text
Development
    ↓
dev Profile
    ↓
Initializer 실행
    ↓
Maintenance Items 생성
```

반면 Production에서는:

```text
Production
    ↓
prod Profile
    ↓
Initializer 실행 안 함
    ↓
maintenance_items = 0
```

이었다.

이는 Profile 분리 관점에서는 의도된 동작이었다.

Production에서 개발용 Initializer를 자동 실행하도록 변경하는 것은
운영 Database의 기준 데이터를 Application Startup에 의존하게 만들 수 있기 때문에
해결 방법으로 선택하지 않았다.

### Resolution

Production Verification을 위해 필요한 정비 기준 데이터를
Amazon RDS에 명시적으로 구성했다.

```text
엔진오일 교환
브레이크 점검
타이어 점검
배터리 점검
냉각수 점검
```

이후 Reservation Flow를 다시 검증하여
정비 항목 조회 및 예약 생성이 정상적으로 동작함을 확인했다.

### Lesson

Application Schema와 Reference Data는
별개의 운영 대상이라는 점을 확인했다.

```text
Schema
→ Table / Column / Constraint

Reference Data
→ 서비스가 동작하기 위해 필요한 기준 데이터
```

`ddl-auto=validate`를 사용하면 Schema 일치 여부는 검증할 수 있지만
서비스에 필요한 기준 데이터까지 보장하지는 않는다.

향후 Production Reference Data는
명시적인 Database Migration 또는 Seed 전략으로 관리할 필요가 있다.

---

## 7. Incident #3 - Login Error Rendering

### Problem

Production 검증 과정에서 Application Log를 확인하던 중
잘못된 로그인 정보를 입력할 경우
Thymeleaf Template Processing Exception이 발생하는 문제를 발견했다.

로그에서는 다음 문제가 확인되었다.

```text
Exception evaluating SpringEL expression:
"error.defaultMessage"

Property or field 'defaultMessage'
cannot be found on object of type 'java.lang.String'
```

기존 Template은 다음과 같았다.

```html
<p th:each="error : ${#fields.globalErrors()}"
   th:text="${error.defaultMessage}">
```

### Root Cause

현재 환경에서 `#fields.globalErrors()`를 순회할 때
Template에서 사용하는 `error`가 문자열 형태로 처리되고 있었다.

따라서:

```text
error
 ↓
String
 ↓
error.defaultMessage
 ↓
Property 없음
 ↓
Template Processing Exception
 ↓
HTTP 500
```

이 발생했다.

정상적인 로그인만 테스트했다면 발견하기 어려운 문제였고,
실패 경로를 실제 Production에서 수행하면서 확인할 수 있었다.

### Fix

관련 수정:

```text
#46
Member | 로그인 실패 오류 메시지 렌더링 수정
```

Template을 다음과 같이 변경했다.

```diff
- th:text="${error.defaultMessage}"
+ th:text="${error}"
```

그리고 동일 문제가 다시 발생하지 않도록
로그인 실패 흐름에 대한 Controller Regression Test를 추가했다.

검증 흐름:

```text
Wrong Credentials
        ↓
POST /members/login
        ↓
LoginFailedException
        ↓
BindingResult Global Error
        ↓
Login Form Rendering
        ↓
HTTP 200
```

Local Controller Test와 전체 Test Suite를 통과시킨 뒤
Fix를 `main`에 병합했다.

### Production Regression

수정된 최신 `main`을 EC2에 반영하고
Production Docker Image를 다시 빌드했다.

```text
Source Update
    ↓
Docker Build
    ↓
Container Recreation
    ↓
Health Check
    ↓
Functional Regression
```

실제 Production에서 잘못된 비밀번호를 다시 입력한 결과:

```text
이메일 또는 비밀번호가 올바르지 않습니다.
```

라는 오류 메시지가 정상적으로 표시되었다.

동시에:

```text
HTTP 500 없음
ERROR / Exception Log 없음
Actuator = UP
Docker = healthy
```

상태를 확인했다.

### Lesson

Happy Path 테스트만으로는
Production 안정성을 충분히 확인할 수 없다는 점을 다시 확인했다.

특히 로그인, Validation, 권한, 오류 화면처럼
사용자가 실패할 수 있는 흐름은
명시적인 Negative Test가 필요하다.

이 경험은 향후 Admin 기능을 구현할 때도
정상적인 ADMIN 접근만 테스트하는 것이 아니라

```text
Unauthenticated
USER
ADMIN
Invalid Resource Owner
Invalid Request
```

와 같은 권한 및 실패 경로를 함께 검증해야 한다는 기준으로 이어진다.

---

## 8. Final Production Verification

#45와 #46 수정 이후
최신 `main`을 Production에 다시 배포했다.

최종 검증 기준 Commit:

```text
3fc20df
Member | 로그인 실패 오류 메시지 렌더링 수정 (#46)
```

Production Verification 결과와 문서는
다음 PR에서 최종 정리했다.

```text
#47
Deployment | GarageCare 1차 운영 배포 검증
```

최종 Production 상태:

```text
Application     UP
Docker          healthy
Amazon RDS      Connected
Functional Test PASS
Regression Test PASS
ERROR Log       None
```

검증 결과는 다음 문서에도 상세히 기록했다.

```text
docs/infrastructure/production-verification.md
```

최종적으로 Sprint 4에서는 다음 전체 사이클을 완료했다.

```text
Infrastructure
      ↓
Deployment
      ↓
Health Verification
      ↓
Functional Verification
      ↓
Incident Detection
      ↓
Root Cause Analysis
      ↓
Fix
      ↓
Regression Test
      ↓
Production Redeployment
      ↓
Production Reverification
```

---

## 9. What Went Well

### Incremental Infrastructure Development

Docker, PostgreSQL, CI, Production Configuration,
RDS, EC2 순서로 환경을 단계적으로 확장한 것이 효과적이었다.

한 번에 모든 Infrastructure를 구성하는 것보다
각 단계에서 발생하는 문제의 범위를 줄일 수 있었다.

### Production-like Verification Before AWS

AWS 배포 전 로컬 Docker 환경에서
`prod` Profile과 PostgreSQL을 사용하여 핵심 흐름을 검증했다.

이 과정이 실제 AWS 배포 전
Production Configuration의 기본적인 문제를 확인하는 안전망 역할을 했다.

### Functional Verification After Deployment

Health Check에서 `UP`이 나온 것을
Production 검증의 종료 조건으로 사용하지 않았다.

실제 사용자 Flow를 수행한 덕분에
Reservation Validation과 Login Error Rendering 문제를 발견할 수 있었다.

### Regression Before Redeployment

Production에서 문제를 발견한 뒤
바로 서버에서 임시 수정하지 않았다.

```text
Issue Detection
    ↓
Local Fix
    ↓
Regression Test
    ↓
PR
    ↓
main Merge
    ↓
Production Redeployment
```

흐름을 유지했다.

이를 통해 Production Server의 Source와
Repository의 `main`이 달라지는 상황을 방지했다.

### Documentation

Deployment, Production Database, Operations,
Production Verification 과정을 문서화했다.

문제 해결 과정도 코드 변경만 남기는 것이 아니라
Issue와 Production Verification 문서에 함께 기록했다.

---

## 10. What Could Be Improved

### Production Reference Data Management

현재 Production Reference Data를 관리하는
명시적인 Migration 전략이 부족하다.

`maintenance_items` 문제를 통해
Application Schema뿐만 아니라 기준 데이터 역시
배포 과정의 일부로 관리해야 한다는 점을 확인했다.

향후 Flyway 등의 Database Migration 도입을 검토할 수 있다.

### Negative Path Testing

로그인 실패 Template 문제는
Production 검증 단계에서 발견되었다.

향후에는 기능 구현 단계부터

```text
Success
Validation Failure
Authentication Failure
Authorization Failure
Resource Not Found
Invalid State Transition
```

등의 실패 경로를 테스트에 포함할 필요가 있다.

### Recovery Verification

Application Container Restart와 재배포 후 복구는 확인했지만,
EC2 Instance 자체의 재부팅 이후 자동 복구는 이번 Sprint에서 수행하지 않았다.

향후 Reliability 검증에서는 다음 흐름을 확인할 필요가 있다.

```text
EC2 Restart
    ↓
Docker Recovery
    ↓
Application Recovery
    ↓
RDS Reconnection
    ↓
Health Recovery
    ↓
Data Integrity Verification
```

### Observability

현재 Actuator Health Check와 Application Logging을 통해
기본적인 운영 상태는 확인할 수 있다.

하지만 장애를 자동으로 감지하고 전달하는 체계는 아직 없다.

향후에는:

```text
Metrics
   ↓
Monitoring
   ↓
Alert
   ↓
Incident Response
```

흐름을 구축할 필요가 있다.

---

## 11. Lessons Learned

Sprint 4에서 가장 중요한 학습은
"배포 성공"과 "운영 가능"이 동일하지 않다는 점이었다.

```text
Application Started
≠
Service Works Correctly
```

Container가 `healthy`이고
Actuator가 `UP`을 반환해도
실제 사용자 Flow에는 문제가 존재할 수 있었다.

따라서 Production 검증은 다음 세 수준을 함께 확인해야 한다.

```text
Infrastructure Health
        +
Application Health
        +
Business Flow
```

또한 Production 장애에 대응할 때
서버에서 직접 임시 수정하기보다

```text
Reproduce
   ↓
Root Cause
   ↓
Code Fix
   ↓
Regression Test
   ↓
Review / Merge
   ↓
Redeploy
   ↓
Production Reverification
```

흐름을 유지하는 것이 중요하다는 점을 경험했다.

마지막으로 정상 흐름만큼 실패 흐름도
Application 품질의 일부라는 점을 확인했다.

```text
Happy Path
+
Failure Path
+
Recovery Path
=
Operational Quality
```

이 기준은 이후 Sprint의 기능 개발과 운영 검증에서도 유지한다.

---

## 12. Remaining Risks

Sprint 4 종료 시점에서 핵심 Production Flow는 정상 동작하지만
다음 운영 과제는 남아 있다.

```text
Production Reference Data Migration
EC2 Recovery Verification
HTTPS
Metrics / Monitoring
Alerting
Audit Logging
Backup / Restore Verification
Security Hardening
```

이 항목들은 Sprint 4의 완료 조건에는 포함하지 않고
후속 Sprint에서 단계적으로 해결한다.

현재 단계에서는 새로운 Infrastructure 기술을
필요 이상으로 추가하지 않는다.

```text
Kubernetes
Microservices
Kafka
```

등은 GarageCare의 현재 규모와 문제에 비해
복잡성을 크게 증가시킬 수 있으므로
명확한 필요가 발생하기 전까지 도입하지 않는다.

---

## 13. Next Steps

### Sprint 5 - Admin & Management

다음 Feature Sprint에서는
실제 정비소 운영을 위한 관리자 기능을 구현한다.

구현 방향:

```text
Authorization Foundation
        ↓
Admin Access Control
        ↓
Reservation Management
        ↓
Member / Vehicle Lookup
        ↓
Maintenance Item Management
        ↓
Admin Dashboard
        ↓
Authorization Regression Test
        ↓
Production Deployment / Verification
```

Sprint 5에서는 단순히 관리자 화면을 추가하는 것이 아니라
USER와 ADMIN의 권한 경계를 명확하게 구현하고 검증하는 것을 중요하게 다룬다.

특히:

```text
Authentication
Authorization
Resource Ownership
Personal Data Exposure
Service Layer Protection
```

을 함께 검토한다.

### Sprint 6 - Reliability & Security

Admin 기능 이후에는
GarageCare의 운영 안정성과 보안을 강화한다.

주요 방향:

```text
Observability
├── Metrics
├── Monitoring
└── Alerting

Security
├── HTTPS
├── Audit Logging
└── Security Hardening

Reliability
├── Application Recovery
├── EC2 Recovery
└── RDS Backup / Restore

Verification
└── Failure → Detection → Response → Recovery
```

단순히 도구를 설치하는 것이 아니라
실제 장애 상황을 재현하고
감지부터 복구까지의 전체 운영 흐름을 검증하는 것을 목표로 한다.

---

## 14. Conclusion

Sprint 4를 통해 GarageCare는

```text
Local Spring Application
```

에서

```text
AWS에서 실행되고
Amazon RDS와 연결되며
Health Check와 Logging을 제공하고
실제 사용자 Flow와 Production Regression을 검증한 서비스
```

로 전환되었다.

Sprint 과정에서 모든 기능이 처음부터 정상 동작한 것은 아니었다.

오히려 실제 Production 환경에서 문제를 발견하고
그 문제를 분석하고 수정한 경험이
이번 Sprint의 가장 중요한 결과였다.

```text
#45
Reservation Vehicle Validation
        ↓
Root Cause
        ↓
Fix
        ↓
Regression
        ↓
Production PASS

Production Seed
        ↓
Profile Difference
        ↓
Root Cause
        ↓
Explicit Production Data
        ↓
Reservation PASS

#46
Login Error Rendering
        ↓
Root Cause
        ↓
Fix
        ↓
Regression
        ↓
Production PASS

#47
Final Production Verification
        ↓
PASS
```

이를 통해 GarageCare의 개발 기준도
단순한 기능 구현에서 다음 단계로 확장되었다.

```text
Implement
   ↓
Test
   ↓
Deploy
   ↓
Observe
   ↓
Detect
   ↓
Fix
   ↓
Verify
```

Sprint 4는 GarageCare의 Infrastructure 구축 단계의 종료이면서,
실제 운영을 고려하는 Backend Application으로 발전하기 위한
기반을 마련한 Sprint로 정리한다.
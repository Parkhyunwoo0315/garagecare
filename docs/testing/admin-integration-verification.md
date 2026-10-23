# Sprint 5 — Admin Integration Verification

> **Project:** GarageCare  
> **Sprint:** Sprint 5 — Admin Management  
> **Status:** Completed  
> **Verification Date:** 2026-10-22  
> **Environment:** Spring Boot / Java / Gradle / H2  
> **Related PR:** #67, #69  
> **Main Commit:** `fb43b80`

---

## 1. Overview

### 1.1 Purpose

본 문서는 GarageCare Sprint 5에서 구현한 관리자 기능의 최종 통합 검증 결과를 기록한다.

검증의 주요 목적은 다음과 같다.

1. 관리자 기능이 정상적으로 동작하는지 확인한다.
2. 비로그인 사용자와 일반 회원의 관리자 접근을 차단한다.
3. 관리자 권한 변경이 기존 세션의 접근 제어에 반영되는지 확인한다.
4. 권한 없는 데이터 변경 요청 이후에도 데이터베이스 상태가 유지되는지 검증한다.
5. 실제 서비스 운영 이전에 추가로 검증해야 할 사항을 식별한다.

본 검증은 로컬 환경의 수동 기능 확인과 Spring Boot 기반 자동화 테스트를 중심으로 진행했다.

운영 환경에서의 부하 테스트, 장애 복구 및 실제 고객 사용 검증은 본 문서의 완료 범위에 포함하지 않는다.

### 1.2 Verification Scope

| Area | Verification Target | Result |
|---|---|---|
| Admin Dashboard | 관리자 대시보드 접근 및 집계 | PASS |
| Member Management | 회원 목록 및 상세 조회 | PASS |
| Vehicle Management | 차량 목록 및 상세 조회 | PASS |
| Maintenance Item Management | 정비 항목 조회 및 상태 변경 권한 | PASS |
| Reservation Management | 예약 조회 및 상태 변경 | PASS |
| Authentication | 비로그인 사용자 접근 제한 | PASS |
| Authorization | MEMBER / ADMIN 권한 구분 | PASS |
| Role Changes | 권한 승격 및 강등 반영 | PASS |
| Data Integrity | 비인가 변경 요청 이후 DB 상태 유지 | PASS |
| Production Operations | 실제 사용자 환경에서의 운영 검증 | NOT VERIFIED |

---

## 2. Verification Environment

### 2.1 Technology Stack

| Component | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot |
| Build Tool | Gradle |
| Web Test | Spring MockMvc |
| Test Framework | JUnit 5 |
| Persistence | Spring Data JPA / Hibernate |
| Test Database | H2 In-Memory Database |
| Production Database | PostgreSQL |
| Deployment | Docker / AWS EC2 |

### 2.2 Local Configuration

관리자 기능의 수동 검증을 위해 `local` 프로필 설정을 추가했다.

**File:** `src/main/resources/application-local.properties`

```properties
# Local verification environment
spring.datasource.url=jdbc:h2:mem:garagecare-local
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=create-drop

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

server.port=8081
```

해당 설정은 H2 인메모리 데이터베이스를 사용하며, 로컬 환경에서 관리자 기능과 데이터 변경 결과를 확인하기 위해 구성했다.

H2 Console 사용을 위해 `build.gradle`에 개발용 의존성을 추가했다.

```gradle
developmentOnly 'org.springframework.boot:spring-boot-h2console'
```

`create-drop` 설정은 로컬 검증용이며 운영 데이터베이스에 적용해서는 안 된다.

### 2.3 Test Execution

검증 과정에서 다음 명령어를 실행했다.

**관리자 권한 회귀 테스트**

```bash
./gradlew test --tests "*AdminAuthorizationRegressionTest"
```

**전체 프로젝트 테스트**

```bash
./gradlew clean test
```

**로컬 CI 테스트**

```bash
./gradlew clean ciTest --no-daemon
```

세 명령 모두 `BUILD SUCCESSFUL` 결과를 확인했다.

---

## 3. Manual Integration Verification

관리자 계정으로 로컬 애플리케이션에 로그인한 뒤 주요 관리자 화면과 예약 처리 흐름을 확인했다.

### 3.1 Admin Dashboard

**Target:** `/admin`

**Verification**

- 관리자 계정으로 대시보드 접근
- 주요 집계 정보 표시 확인
- 관리자 화면 렌더링 확인

**Result:** PASS

### 3.2 Member Management

**Verification**

- 관리자 회원 목록 조회
- 회원 상세 정보 조회
- 회원 정보 및 등록 차량 관련 화면 확인

**Result:** PASS

### 3.3 Vehicle Management

**Verification**

- 전체 등록 차량 목록 조회
- 차량 상세 정보 조회
- 차량과 회원 정보의 연결 확인

**Result:** PASS

### 3.4 Maintenance Item Management

**Verification**

- 관리자 정비 항목 목록 조회
- 정비 항목 관리 화면 확인

정비 항목의 등록·수정·활성화·비활성화 기능은 별도의 Controller 및 Service 자동화 테스트에서 정상 동작을 검증했다.

**Result:** PASS

### 3.5 Reservation Management

관리자 화면에서 예약 조회 및 상태 변경 흐름을 확인했다.

**Verified Flow**

```text
PENDING
   |
   | Confirm
   v
CONFIRMED
   |
   | Complete
   v
COMPLETED
```

**Verification**

- 관리자 예약 목록 조회
- 예약 상세 정보 조회
- PENDING 상태의 예약 확정
- CONFIRMED 상태의 예약 완료
- 상태 변경 이후 관리자 화면 반영 확인

**Result:** PASS

---

## 4. Authentication & Authorization Verification

### 4.1 Authorization Policy

GarageCare 관리자 기능은 `/admin/**` 경로에 접근하는 사용자의 로그인 여부와 관리자 권한을 확인한다.

검증 대상 정책은 다음과 같다.

| User Type | Expected Behavior |
|---|---|
| Guest | 로그인 페이지로 리다이렉트 |
| MEMBER | HTTP 403 Forbidden |
| ADMIN | 관리자 기능 접근 허용 |
| Invalid Member ID | 관리자 접근 차단 |
| Demoted ADMIN | 기존 세션에서도 접근 차단 |
| Promoted MEMBER | 관리자 접근 허용 |

### 4.2 Authentication Verification

**Target:** `LoginCheckInterceptor`

비로그인 사용자가 관리자 기능에 접근할 경우 로그인 페이지로 이동하는지 확인했다.

세션이 존재하더라도 로그인 회원 정보가 없는 경우 보호된 요청이 허용되지 않는지도 검증했다.

**Result:** PASS

### 4.3 Authorization Verification

**Target:** `AdminAuthorizationInterceptor`

일반 회원이 관리자 페이지 및 데이터 변경 요청에 접근할 경우 HTTP 403 응답을 반환하는지 확인했다.

ADMIN 권한을 가진 회원은 정상적으로 접근할 수 있는지 검증했다.

**Result:** PASS

### 4.4 Role Change Verification

회원 권한 변경 이후 기존 세션에서의 관리자 접근 가능 여부를 검증했다.

**Scenario A — ADMIN → MEMBER**

1. ADMIN 회원 생성
2. 관리자 접근 허용 확인
3. DB에 저장된 회원 권한을 MEMBER로 변경
4. 기존 세션으로 관리자 페이지 접근
5. HTTP 403 응답 확인

**Result:** PASS

**Scenario B — MEMBER → ADMIN**

1. MEMBER 회원 생성
2. 관리자 접근 차단 확인
3. DB에 저장된 회원 권한을 ADMIN으로 변경
4. 기존 세션으로 관리자 페이지 접근
5. 관리자 접근 허용 확인

**Result:** PASS

권한 검증 시 세션에 저장된 회원 ID를 사용하되, 현재 데이터베이스의 회원 권한을 기준으로 접근을 판단하는 동작을 확인했다.

---

## 5. Data Integrity Verification

관리자 권한이 없는 사용자가 데이터 변경 요청을 실행했을 때 HTTP 요청이 차단되는 것뿐 아니라 실제 데이터베이스 상태도 유지되는지 검증했다.

### 5.1 Maintenance Item

| Scenario | Expected Response | Expected DB State | Result |
|---|---|---|---|
| MEMBER — Activate | 403 Forbidden | `active = false` | PASS |
| MEMBER — Deactivate | 403 Forbidden | `active = true` | PASS |

추가적으로 기존 `AdminMutationAuthorizationTest`에서 정비 항목 등록 및 수정 요청 차단과 데이터 유지 여부를 검증했다.

### 5.2 Reservation

| Scenario | Expected Response | Expected DB State | Result |
|---|---|---|---|
| MEMBER — Confirm | 403 Forbidden | PENDING | PASS |
| MEMBER — Complete | 403 Forbidden | CONFIRMED | PASS |
| Guest — Confirm | Login Redirect | PENDING | PASS |
| Demoted ADMIN — Confirm | 403 Forbidden | PENDING | PASS |

### 5.3 Verification Method

데이터 무결성 검증은 다음 순서로 진행했다.

1. 테스트용 회원 및 대상 엔티티 생성
2. 초기 데이터 상태 설정
3. 비인가 관리자 요청 실행
4. HTTP 응답 확인
5. 영속성 컨텍스트 정리
6. Repository를 통한 데이터 재조회
7. 요청 전후 데이터 상태 비교

테스트에서는 `EntityManager.flush()` 및 `EntityManager.clear()`를 사용하여 영속성 컨텍스트의 캐시 상태가 검증 결과에 영향을 주지 않도록 했다.

**Result:** PASS

---

## 6. Automated Test Results

### 6.1 Regression Test

**Test Class:** `AdminAuthorizationRegressionTest`

관리자 인증·인가 및 데이터 무결성 관련 15개 테스트의 통과를 확인했다.

**Result:** PASS

### 6.2 Existing Authorization Tests

기존 테스트 클래스에서도 관리자 접근 및 데이터 변경 권한을 검증했다.

- `AdminAuthorizationInterceptorTest`
- `AdminAuthorizationWebTest`
- `AdminMutationAuthorizationTest`
- `LoginCheckInterceptorTest`

**Result:** PASS

### 6.3 Functional Tests

전체 테스트 실행 과정에서 다음 영역의 테스트 통과를 확인했다.

- 관리자 Dashboard Controller / Service
- 관리자 Member Controller
- 관리자 Vehicle Controller
- 관리자 Maintenance Item Controller
- 관리자 Reservation Controller
- Member Controller / Service / Repository
- Vehicle Controller / Service
- Reservation Controller / Service / Repository
- Maintenance Item Service
- Reservation Domain
- Reservation N+1 조회 검증

**Result:** PASS

### 6.4 CI Execution

**Command**

```bash
./gradlew clean ciTest --no-daemon
```

**Result**

```text
BUILD SUCCESSFUL in 9s
6 actionable tasks: 6 executed
```

테스트 실행 로그에서 개별 테스트의 `PASSED` 결과를 확인했다.

테스트 종료 과정에서 출력된 Hibernate의 `drop table if exists` 로그와 HikariCP 종료 로그는 테스트용 데이터베이스 및 애플리케이션 컨텍스트 정리 과정에서 발생한 정상적인 종료 로그로 판단했다.

`PostgreSqlSchemaTest.java`의 unchecked operation 컴파일 경고가 출력되었으나 테스트 실패는 발생하지 않았다.

본 결과는 로컬에서 실행한 CI 테스트 결과이며, GitHub Actions의 원격 실행 결과를 별도로 증명하는 것은 아니다.

---

## 7. Changes & Traceability

### 7.1 Verification Changes

이번 최종 검증 작업에서 다음 파일을 변경했다.

| File | Description |
|---|---|
| `AdminAuthorizationRegressionTest.java` | 관리자 권한 및 데이터 무결성 회귀 테스트 강화 |
| `build.gradle` | H2 Console 개발용 의존성 추가 |
| `application-local.properties` | 로컬 H2 검증 환경 구성 |

### 7.2 Related Commits

| Commit | Description |
|---|---|
| `38a40a7` | 관리자 권한 및 데이터 무결성 회귀 테스트 강화 |
| `88be4e8` | 로컬 H2 개발 환경 설정 추가 |
| `fb43b80` | Sprint 5 관리자 기능 최종 통합 검증 PR 병합 |

### 7.3 Related Pull Requests

- PR #67 — 관리자 권한 회귀 테스트
- PR #69 — Sprint 5 관리자 기능 최종 통합 검증

---

## 8. Limitations & Follow-up

이번 검증으로 관리자 기능의 주요 접근 제어와 데이터 변경 차단 동작을 확인했다.

다만 실제 사용자 운영을 위해서는 다음 항목에 대한 추가 검증이 필요하다.

| Area | Follow-up | Priority |
|---|---|---|
| Production Deployment | 최신 관리자 기능의 운영 환경 반영 및 검증 | High |
| Database | PostgreSQL 백업 및 복구 절차 검증 | High |
| Reservation | 중복 예약 및 동시 요청 처리 검증 | High |
| Security | HTTPS, 세션 및 운영 관리자 계정 보안 점검 | High |
| Privacy | 개인정보 수집·보관·삭제 정책 점검 | High |
| Monitoring | 운영 로그, 장애 감지 및 대응 절차 검증 | High |
| Usability | 실제 정비소 관리자 업무 흐름 검증 | High |
| User Feedback | 제한적 고객 대상 파일럿 운영 | High |
| Performance | 실제 운영 환경에서의 성능 및 부하 검증 | Medium |

위 항목은 본 문서에서 완료된 검증으로 간주하지 않는다.

특히 자동화 테스트에서 사용하는 H2와 실제 운영 환경의 PostgreSQL은 데이터베이스 종류가 다르므로, 운영 환경에서 별도의 통합 검증이 필요하다.

---

## 9. Conclusion

Sprint 5에서는 GarageCare 관리자 기능의 주요 업무 흐름과 인증·인가 정책을 검증했다.

관리자 대시보드, 회원, 차량, 정비 항목 및 예약 관리 기능의 수동 검증을 수행했으며, 자동화 테스트를 통해 비로그인 사용자와 일반 회원의 관리자 접근 차단을 확인했다.

또한 관리자 권한 승격·강등 시나리오와 비인가 데이터 변경 요청 이후의 데이터 무결성을 검증했다.

최종적으로 다음 결과를 확인했다.

- 관리자 주요 기능 수동 검증 완료
- 관리자 인증·인가 회귀 테스트 통과
- 권한 변경 시나리오 검증 완료
- 비인가 변경 요청에 대한 데이터 무결성 검증 완료
- 전체 프로젝트 테스트 통과
- 로컬 CI 테스트 통과
- 최종 검증 변경 사항 `main` 브랜치 병합 완료

**Sprint 5의 관리자 기능 통합 검증 목표는 달성한 것으로 판단한다.**

다음 단계에서는 새로운 기능 구현보다 실제 운영 환경의 안정성과 예약 처리 신뢰성을 검증하고, 제한된 실사용자 대상 파일럿 운영을 추진한다.

---

## 10. Next Milestone

**Sprint 6 — Pilot Launch & Real-World Operations**

### Objective

GarageCare를 실제 정비소 업무에 적용하고, 제한된 사용자 대상 파일럿 운영을 통해 서비스 안정성 및 사용성을 검증한다.

### Expected Outcomes

- 운영 환경 최신 버전 배포
- 운영 보안 및 데이터 보호 점검
- 예약 중복·동시성 검증
- 데이터베이스 백업 및 복구 절차 확인
- 실제 정비소 관리자 업무 흐름 검증
- 제한된 고객 대상 예약 운영
- 운영 이슈 및 사용자 피드백 수집
- 개선 사항 도출 및 후속 작업 계획 수립
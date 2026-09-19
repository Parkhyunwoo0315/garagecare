# Admin Authorization Policy

> Sprint: Sprint 5 - Admin & Management  
> Status: Design  
> Last Updated: 2026-09-19

---

## 1. Overview

GarageCare Sprint 5에서는 실제 정비소 운영을 위한
관리자 기능을 구현한다.

관리자 기능은 일반 회원보다 넓은 범위의 데이터에 접근하기 때문에
관리 화면을 구현하기 전에 Authentication과 Authorization 정책을 명확하게 정의한다.

이번 정책의 핵심 목표는 다음과 같다.

```text
Authentication
    ↓
Who are you?

Authorization
    ↓
What are you allowed to do?
```

GarageCare는 현재 Session 기반 Authentication을 사용한다.

로그인 성공 시 Session에는 다음 값이 저장된다.

```text
LOGIN_MEMBER_ID
        ↓
memberId
```

현재 `LoginCheckInterceptor`는 Session에
`LOGIN_MEMBER_ID`가 존재하는지만 확인한다.

따라서 현재 구조는:

```text
Unauthenticated
      ↓
Protected URL
      ↓
Login Redirect

Authenticated MEMBER
      ↓
Protected URL
      ↓
ALLOW

Authenticated ADMIN
      ↓
Protected URL
      ↓
ALLOW
```

상태이다.

즉 `/admin/**` 역시 로그인 여부만 확인할 뿐
ADMIN Role은 아직 검증하지 않는다.

Sprint 5에서는 이 구조를 확장하여
Authentication과 Authorization의 책임을 분리한다.

---

## 2. Current Authentication Architecture

현재 GarageCare의 로그인 흐름은 다음과 같다.

```text
POST /members/login
        ↓
MemberService.login()
        ↓
memberId
        ↓
HttpSession
        ↓
LOGIN_MEMBER_ID = memberId
```

Session에는 Member Entity 전체나 Role을 저장하지 않고
회원 ID만 저장한다.

```java
session.setAttribute(
    SessionConst.LOGIN_MEMBER_ID,
    memberId
);
```

현재 보호 대상 URL은 다음과 같다.

```text
/reservations/**
/vehicles/**
/mypage/**
/admin/**
```

`LoginCheckInterceptor`는 요청마다 Session을 확인한다.

```text
Session 없음
      OR
LOGIN_MEMBER_ID 없음
        ↓
Login Redirect

LOGIN_MEMBER_ID 존재
        ↓
Request 허용
```

이 구조는 Authentication에는 사용할 수 있지만
ADMIN Authorization을 처리하기에는 충분하지 않다.

---

## 3. Role Model

GarageCare는 현재 다음 Role을 사용한다.

```java
public enum MemberRole {
    MEMBER,
    ADMIN
}
```

따라서 Sprint 5에서도 새로운 Role을 추가하지 않고
기존 `MemberRole`을 그대로 사용한다.

### MEMBER

일반 GarageCare 사용자이다.

기본 원칙:

```text
자신의 Resource만 접근 가능
```

주요 접근 대상:

```text
Own Vehicle
Own Reservation
Own Member Information
```

관리자 Resource에는 접근할 수 없다.

```text
/admin/** → DENY
```

### ADMIN

GarageCare 운영 관리자이다.

정비소 운영을 위해 여러 회원의 데이터를
조회하거나 관리할 수 있다.

주요 관리 대상:

```text
Reservation
Member
Vehicle
MaintenanceItem
```

단, ADMIN이라는 이유만으로
모든 Domain Entity에 무제한 수정 권한을 부여하지 않는다.

각 관리 기능에서 실제 운영에 필요한 최소 권한만 부여한다.

---

## 4. Authentication Policy

GarageCare는 Sprint 5에서도
현재 Session 기반 Authentication 구조를 유지한다.

Sprint 5에서 Spring Security로
Authentication 구조 전체를 교체하지 않는다.

현재 구조:

```text
Login
  ↓
MemberService
  ↓
memberId
  ↓
Session
  ↓
LOGIN_MEMBER_ID
```

를 유지한다.

Session에는 현재와 동일하게
`memberId`를 Authentication Identity로 저장한다.

Role은 Authorization이 필요한 시점에
서버가 신뢰할 수 있는 Member 정보에서 확인한다.

이를 통해 Session에 저장된 Role 값과
Database의 실제 Role이 달라지는 문제를 피하고,
Role 변경 사항이 Authorization에 반영될 수 있도록 한다.

---

## 5. URL Authorization Policy

URL 접근 정책은 두 단계로 분리한다.

```text
Authentication
        ↓
Authorization
```

### Authentication

현재 `LoginCheckInterceptor`가 담당한다.

```text
/reservations/**
/vehicles/**
/mypage/**
/admin/**
```

에 접근하는 사용자가 로그인 상태인지 확인한다.

### Admin Authorization

`/admin/**` 요청은 로그인 여부 확인 이후
추가로 `MemberRole.ADMIN` 여부를 검증해야 한다.

최종 흐름:

```text
/admin/**
    ↓
LoginCheckInterceptor
    ↓
Authenticated?
    │
    ├─ NO
    │    ↓
    │ Login Redirect
    │
    └─ YES
         ↓
      Admin Authorization
         ↓
      MemberRole == ADMIN?
         │
         ├─ NO → DENY
         │
         └─ YES → ALLOW
```

따라서 단순히 로그인한 MEMBER가
`/admin/**` URL을 직접 입력하여
관리 기능에 접근할 수 없어야 한다.

---

## 6. Resource Authorization

URL Authorization만으로
GarageCare의 권한 정책을 완성하지 않는다.

Resource 자체의 소유권과 관리 권한도 검증한다.

### MEMBER Vehicle

MEMBER는 자신의 Vehicle만 접근할 수 있다.

```text
Authenticated Member ID
          ↓
Vehicle.member.id
          ↓
Same?
     ┌────┴────┐
    YES        NO
     ↓          ↓
   ALLOW       DENY
```

### MEMBER Reservation

MEMBER는 자신의 Reservation만 접근할 수 있다.

```text
Authenticated Member ID
          ↓
Reservation.member.id
          ↓
Same?
     ┌────┴────┐
    YES        NO
     ↓          ↓
   ALLOW       DENY
```

URL에 다른 회원의 Resource ID를 직접 입력하는 방식으로
Ownership 검증을 우회할 수 없어야 한다.

### Existing Ownership Protection

GarageCare에는 이미 일부 Domain에서
Resource Ownership 검증이 구현되어 있다.

Vehicle Domain은 회원 ID를 기준으로
차량 소유 여부를 확인할 수 있다.

```text
Vehicle
   ↓
isOwnedBy(memberId)
```

Reservation Service 역시
Reservation의 Member와 현재 로그인 Member를 비교하여
소유권을 검증하는 흐름을 사용한다.

따라서 Sprint 5에서는 기존 Ownership 정책을 제거하거나
Admin Authorization과 혼합하지 않는다.

```text
MEMBER
   ↓
Existing Ownership Policy
   ↓
Own Resource Only
```

관리자 기능은 기존 MEMBER Ownership 정책 위에
별도의 ADMIN 관리 권한을 추가하는 방향으로 구현한다.

```text
Existing MEMBER Ownership
            +
ADMIN Management Authorization
```

이를 통해 관리자 기능 추가로 인해
기존 MEMBER Resource 보호가 약화되지 않도록 한다.

---

## 7. Admin Resource Policy

ADMIN은 서비스 운영에 필요한 범위에서
다른 회원의 Resource에 접근할 수 있다.

기본 정책은 다음과 같다.

| Resource | MEMBER | ADMIN |
|---|---|---|
| Own Member Information | READ | READ |
| Other Member Information | DENY | READ |
| Own Vehicle | READ / CREATE | READ |
| Other Vehicle | DENY | READ |
| Own Reservation | READ / CREATE / CANCEL | READ / UPDATE |
| Other Reservation | DENY | READ / UPDATE |
| MaintenanceItem | READ | READ / CREATE / UPDATE |
| Admin Dashboard | DENY | READ |

ADMIN의 권한은 운영상 필요한 범위로 제한한다.

예를 들어 Member나 Vehicle은
관리자에게 기본적으로 조회 권한만 제공한다.

```text
Member
ADMIN → READ

Vehicle
ADMIN → READ
```

회원 정보나 차량 정보를 관리자가 임의로 수정하는 기능은
실제 운영 요구가 확인되기 전까지 추가하지 않는다.

---

## 8. Web Layer Responsibility

Web Layer는 요청 수준의
Authentication과 Role 기반 접근 제어를 담당한다.

```text
HTTP Request
      ↓
Authentication Check
      ↓
Role Check
      ↓
Controller
```

책임:

```text
로그인 여부 확인
/admin/** ADMIN 여부 확인
허용되지 않은 요청 차단
적절한 Redirect / Error 처리
```

하지만 Web Layer만으로
Resource Authorization을 완성하지 않는다.

---

## 9. Service Layer Responsibility

Service Layer는 Business Resource에 대한
Authorization을 담당한다.

주요 책임:

```text
Resource Ownership
Admin-only Operation
Business Authorization
```

예를 들어 Reservation 조회가 필요한 경우:

```text
memberId
reservationId
      ↓
Reservation 조회
      ↓
Ownership 확인
      ↓
Business Logic
```

방식을 사용한다.

Controller가 잘못 호출되거나
향후 새로운 Endpoint가 추가되더라도
Service Layer의 핵심 권한 규칙이 유지되도록 한다.

즉 GarageCare의 Authorization은 다음과 같이 구성한다.

```text
Request
   ↓
Web Authentication
   ↓
Web Role Authorization
   ↓
Controller
   ↓
Service Resource Authorization
   ↓
Repository
```

---

## 10. Privacy Policy

ADMIN은 서비스 운영을 위해
회원 정보를 조회할 수 있지만
필요 이상의 개인정보를 노출하지 않는다.

관리자 화면에서는 Entity를 직접 전달하지 않고
관리 목적에 맞는 DTO 사용을 기본 원칙으로 한다.

```text
Member Entity
      ↓
Admin Member DTO
      ↓
Required Fields Only
```

다음 정보는 관리자 UI 또는 API에 노출하지 않는다.

```text
Password
Password Hash
Authentication Internal Data
Unnecessary Internal Fields
```

회원 정보는 정비소 운영에 필요한 범위에서만 제공한다.

---

## 11. Failure Policy

### Unauthenticated

보호된 Resource에 비로그인 사용자가 접근하면
로그인 페이지로 Redirect한다.

```text
Protected Resource
       ↓
Unauthenticated
       ↓
/members/login?redirectURL=...
```

현재 `LoginCheckInterceptor` 정책을 유지한다.

### Authenticated MEMBER → Admin Resource

로그인한 MEMBER가 `/admin/**`에 접근하는 것은
Authentication Failure가 아니라 Authorization Failure이다.

따라서 Login Page로 다시 보내는 방식과
구분하여 처리한다.

기본 정책:

```text
Authenticated MEMBER
        ↓
/admin/**
        ↓
Authorization Failure
        ↓
Access Denied
```

구체적인 HTTP Status 또는 Error Page 표현은
Admin Access Control 구현 이슈에서
현재 MVC Error 처리 구조와 함께 결정한다.

### Resource Ownership Failure

MEMBER가 다른 회원의 Resource에 접근하려는 경우
해당 Resource의 상세 정보가 노출되지 않아야 한다.

Resource 존재 여부 자체가
불필요하게 노출되지 않도록 처리한다.

---

## 12. Authorization Matrix

Sprint 5 Authorization Test는
다음 Matrix를 기본 기준으로 사용한다.

| Resource | Unauthenticated | MEMBER | ADMIN |
|---|---|---|---|
| Public Page | ALLOW | ALLOW | ALLOW |
| Own Vehicle | DENY | ALLOW | ALLOW |
| Other Vehicle | DENY | DENY | ALLOW |
| Own Reservation | DENY | ALLOW | ALLOW |
| Other Reservation | DENY | DENY | ALLOW |
| Admin Dashboard | DENY | DENY | ALLOW |
| Reservation Management | DENY | DENY | ALLOW |
| Member Management | DENY | DENY | ALLOW |
| Vehicle Management | DENY | DENY | ALLOW |
| MaintenanceItem Management | DENY | DENY | ALLOW |

이 Matrix는 후속 기능이 추가될 때
Authorization Regression Test의 기준으로 유지한다.

---

## 13. Test Strategy

Authorization은 정상적인 ADMIN 접근만 테스트하지 않는다.

다음 세 사용자 상태를 기본 Test Fixture로 사용한다.

```text
Unauthenticated
MEMBER
ADMIN
```

### Authentication Test

```text
Unauthenticated
      ↓
Protected URL
      ↓
Login Redirect
```

### MEMBER Admin Access Test

```text
MEMBER
   ↓
/admin/**
   ↓
DENY
```

### ADMIN Access Test

```text
ADMIN
   ↓
/admin/**
   ↓
ALLOW
```

### Ownership Test

```text
MEMBER A
   ↓
MEMBER A Resource
   ↓
ALLOW
```

```text
MEMBER A
   ↓
MEMBER B Resource
   ↓
DENY
```

### Regression

Admin 기능이 추가될 때마다
Authorization Matrix를 기준으로
기존 권한 정책이 깨지지 않는지 확인한다.

---

## 14. Admin Account Policy

현재 일반 회원가입은 `Member.create()`를 통해
항상 `MemberRole.MEMBER` 계정을 생성한다.

```text
Public Signup
      ↓
Member.create()
      ↓
MemberRole.MEMBER
```

따라서 현재 Application에는
일반 사용자가 회원가입 Request를 조작하여
`ADMIN` Role을 획득하는 경로가 존재하지 않는다.

이 정책을 Sprint 5에서도 유지한다.

```text
Public Signup
      ↓
MEMBER

Public Role Change
      ↓
DENY
```

일반 사용자에게 다음 기능을 제공하지 않는다.

```text
ADMIN 회원가입
Role 선택
Role 변경 Endpoint
MEMBER → ADMIN 승격 Endpoint
```

현재 Application에는 별도의 관리자 생성 기능이 없으므로
초기 ADMIN 계정 Provisioning은 일반 회원가입과 분리된
신뢰된 운영 절차를 사용한다.

구체적인 Production ADMIN Provisioning 방식은
Admin Access Control 구현 단계에서 결정한다.

관리자 권한 부여는 일반 사용자 기능이 아니라
운영 관리 작업으로 취급한다.

---

## 15. Security Principles

Sprint 5 Authorization 구현에서는
다음 원칙을 따른다.

### Default Deny

명시적으로 허용되지 않은
관리자 Resource 접근은 거부한다.

### Least Privilege

ADMIN에게도 실제 운영에 필요한
최소한의 권한만 제공한다.

### Server-side Authorization

Client 또는 Template의 UI 숨김을
Authorization으로 간주하지 않는다.

```text
Button Hidden
≠
Authorization
```

모든 권한 검증은 서버에서 수행한다.

### Defense in Depth

Authorization을 하나의 Layer에만 의존하지 않는다.

```text
Web Layer
+
Service Layer
+
Ownership Validation
```

을 통해 권한 우회를 방지한다.

---

## 16. Out of Scope

이번 Authorization Policy에서는
다음 항목을 구현하지 않는다.

```text
Spring Security Migration
OAuth / OIDC
JWT
Multi-factor Authentication
Rate Limiting
Audit Logging
HTTPS
Advanced RBAC
Permission Table
```

현재 GarageCare 규모에서는

```text
MEMBER
ADMIN
```

두 Role 기반의 단순하고 명확한 Authorization Model을 유지한다.

필요성이 확인되지 않은 보안 기술을
Sprint 5에 추가하지 않는다.

---

## 17. Implementation Direction

정책 문서 승인 이후
다음 순서로 구현한다.

```text
Current Authentication Review
        ↓
Admin Authorization Component
        ↓
/admin/** Access Control
        ↓
MEMBER / ADMIN Tests
        ↓
Reservation Management
        ↓
Member / Vehicle Management
        ↓
MaintenanceItem Management
        ↓
Admin Dashboard
        ↓
Authorization Regression Test
        ↓
Production Verification
```

첫 번째 구현 단계에서는
현재 `LoginCheckInterceptor`의 Authentication 책임을 유지하면서
ADMIN Authorization을 별도의 책임으로 분리한다.

이를 통해 하나의 Interceptor가
Authentication과 Authorization을 모두 담당하는 구조를 피한다.

---

## 18. Conclusion

Sprint 5의 관리자 기능은
관리 화면을 만드는 것부터 시작하지 않는다.

먼저 다음 권한 경계를 확립한다.

```text
Unauthenticated
      ↓
Authentication Required

MEMBER
      ↓
Own Resource Only

ADMIN
      ↓
Management Resource
```

그리고 권한 검증을 다음 구조로 분리한다.

```text
Web Layer
   ↓
Authentication / Role

Service Layer
   ↓
Resource / Ownership
```

이 정책을 Sprint 5 전체 관리자 기능의
Authorization 기준으로 사용한다.
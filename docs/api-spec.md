# GarageCare API Specification

> Version: 1.0.0  
> Status: Draft  
> Last Updated: 2026-07-24

---

# 1. Overview

본 문서는 GarageCare MVP에서 사용하는 API를 정의한다.

API는 클라이언트와 서버 간의 데이터 교환 규칙을 명확하게 정의하며,
Controller 구현, DTO 설계, Validation, 권한 처리 및 통합 테스트의 기준 문서로 사용한다.

본 프로젝트는 Spring Boot와 Thymeleaf 기반의 MVC 애플리케이션으로 구현되지만,
향후 REST API 또는 SPA(Vue, React)로 확장될 수 있도록 API 중심으로 설계한다.

---

# 2. Scope

현재 API 명세는 GarageCare MVP 범위를 대상으로 한다.

## Included

### Authentication

- 로그인
- 로그아웃
- 현재 로그인 사용자 조회

### Member

- 회원가입

### Vehicle

- 차량 목록
- 차량 등록
- 차량 상세
- 차량 수정

### Reservation

- 예약 생성
- 예약 목록
- 예약 상세
- 예약 취소
- 관리자 예약 조회
- 예약 상태 변경

### MaintenanceItem

- 정비 항목 조회
- 정비 항목 등록
- 수정
- 활성·비활성 변경

### Notice

- 공지사항 조회
- 등록
- 수정
- 삭제

---

## Out of Scope

다음 기능은 MVP 이후 별도의 API를 설계한다.

- 비밀번호 변경
- 회원 탈퇴
- 결제
- 정비 이력
- 알림
- AI 상담
- 다중 정비소
- 파일 업로드
- 이미지 관리

---

# 3. API Design Principles

GarageCare의 API는 다음 원칙을 따른다.

---

## 3.1 Resource-Oriented

API는 화면이 아닌 리소스를 기준으로 설계한다.

예를 들어

Good

```
GET /vehicles
```

Bad

```
GET /vehicleList
```

---

## 3.2 HTTP Method

| Method | Purpose |
|---------|---------|
| GET | 조회 |
| POST | 생성 |
| PUT | 전체 수정 |
| PATCH | 일부 수정 |
| DELETE | 삭제 |

GarageCare에서는 삭제 대신 상태 변경을 사용하는 경우가 많으므로 DELETE 사용은 최소화한다.

예)

```
PATCH /reservations/{id}/cancel
```

---

## 3.3 URL Naming

모든 URL은

- 소문자
- 복수형
- kebab-case

를 사용한다.

Good

```
/maintenance-items
```

Bad

```
/MaintenanceItem
/maintenanceItem
```

---

## 3.4 Stateless

HTTP 요청은 각각 독립적으로 처리된다.

인증은 Spring Security Session 기반으로 구현하지만,
API 설계 자체는 Stateless한 요청 구조를 유지한다.

---

## 3.5 Consistency

모든 API는 동일한 규칙을 따른다.

- 동일한 Response 구조
- 동일한 Error 구조
- 동일한 Status Code
- 동일한 Validation 방식

---

## 3.6 Predictability

URL만 보아도 동작을 예측할 수 있어야 한다.

예)

```
GET /vehicles

차량 목록 조회
```

```
POST /vehicles

차량 등록
```

```
GET /vehicles/{vehicleId}

차량 상세 조회
```

---

# 4. Naming Convention

---

## Controller

```
MemberController
VehicleController
ReservationController
NoticeController
```

---

## Request DTO

```
MemberCreateRequest
VehicleCreateRequest
ReservationCreateRequest
```

---

## Response DTO

```
VehicleResponse
ReservationResponse
NoticeResponse
```

---

## Path Variable

항상 Resource 이름을 사용한다.

Good

```
vehicleId
reservationId
noticeId
```

Bad

```
id
```

---

## Query Parameter

camelCase 사용

```
status
page
size
sort
reservationDate
```

---

# 5. Common Request Rule

---

## Content-Type

```
application/json
```

Form 기반 MVC에서도 Controller 내부 DTO 구조를 기준으로 작성한다.

---

## Character Encoding

```
UTF-8
```

---

## Date Format

```
yyyy-MM-dd
```

예)

```
2026-07-25
```

---

## Time Format

```
HH:mm
```

예)

```
10:30
```

---

## DateTime Format

```
yyyy-MM-dd'T'HH:mm:ss
```

예)

```
2026-07-25T10:30:00
```

---

## Pagination

```
?page=0
&size=10
```

MVP에서는 일부 화면만 Pagination을 적용한다.

---

# 6. Authentication

GarageCare는 Session 기반 인증을 사용한다.

로그인 후 Session에 사용자 정보를 저장하며,
인증이 필요한 요청은 로그인 여부를 확인한다.

---

## Authorization

| Role | Description |
|------|-------------|
| GUEST | 비회원 |
| CUSTOMER | 일반 회원 |
| ADMIN | 관리자 |

---

# 7. Authentication API

---

# 7.1 Login

## Purpose

회원 로그인

---

## URL

```
POST /login
```

---

## Authorization

```
Permit All
```

---

## Request

```json
{
  "loginId": "parkhyunwoo",
  "password": "1234"
}
```

---

## Validation

| Field | Rule |
|---------|---------|
| loginId | Required |
| password | Required |

---

## Business Rules

- 아이디 존재 여부 확인
- 비밀번호 검증
- Session 생성
- 로그인 사용자 저장

---

## Success Response

HTTP

```
302 Found
```

Redirect

```
/
```

또는

```
/admin
```

관리자 로그인 시 관리자 페이지로 이동한다.

---

## Error Response

```
401 Unauthorized
```

```json
{
  "code": "AUTH_001",
  "message": "아이디 또는 비밀번호를 확인해 주세요."
}
```

---

# 7.2 Logout

## Purpose

로그아웃

---

## URL

```
POST /logout
```

---

## Authorization

```
Authenticated
```

---

## Business Rules

- Session 제거
- 인증 정보 제거

---

## Success

```
302 Found
```

Redirect

```
/
```

---

# 7.3 Current User

## Purpose

현재 로그인한 사용자 조회

---

## URL

```
GET /api/me
```

---

## Authorization

```
Authenticated
```

---

## Success

```json
{
  "id": 1,
  "name": "박현우",
  "role": "CUSTOMER"
}
```

---

## Error

```
401 Unauthorized
```

---

# 8. Member API

---

# 8.1 Create Member

## Purpose

회원가입

---

## URL

```
POST /members
```

---

## Authorization

```
Permit All
```

---

## Request

```json
{
  "loginId":"parkhyunwoo",
  "password":"1234",
  "confirmPassword":"1234",
  "name":"박현우",
  "phone":"01012345678"
}
```

---

## Validation

| Field | Rule |
|---------|---------|
| loginId | Required |
| loginId | Duplicate Check |
| password | Required |
| confirmPassword | Must Match |
| name | Required |
| phone | Phone Format |

---

## Business Rules

- 로그인 아이디 중복 불가
- 비밀번호 암호화 저장
- 기본 권한 CUSTOMER
- 회원 생성 후 로그인하지 않음

---

## Success

HTTP

```
201 Created
```

Response

```json
{
  "id":1,
  "message":"회원가입이 완료되었습니다."
}
```

---

## Error

Duplicate LoginId

```json
{
  "code":"MEMBER_001",
  "message":"이미 사용 중인 로그인 아이디입니다."
}
```

Password

```json
{
  "code":"MEMBER_002",
  "message":"비밀번호가 일치하지 않습니다."
}
```

Validation

```json
{
  "code":"COMMON_001",
  "message":"입력값을 확인해 주세요."
}
```

---

# 9. Common Response Format

GarageCare는 모든 API에서 동일한 Response 구조를 사용한다.

---

## Success

```json
{
  "success": true,
  "data": {
  },
  "message": "성공"
}
```

---

## Fail

```json
{
  "success": false,
  "code": "MEMBER_001",
  "message": "이미 사용 중인 로그인 아이디입니다."
}
```

---

# 10. HTTP Status Policy

| Status | Meaning |
|---------|---------|
| 200 | 조회 성공 |
| 201 | 생성 성공 |
| 204 | 성공 (응답 없음) |
| 400 | Validation 실패 |
| 401 | 로그인 필요 |
| 403 | 권한 없음 |
| 404 | 데이터 없음 |
| 409 | 중복 데이터 |
| 500 | 서버 오류 |

---

# 11. Error Code Convention

모든 Error Code는

```
도메인_번호
```

형태를 사용한다.

예)

```
AUTH_001
MEMBER_001
VEHICLE_001
RESERVATION_001
NOTICE_001
COMMON_001
```

---

## Common

| Code | Description |
|------|-------------|
| COMMON_001 | Validation 실패 |
| COMMON_002 | 요청 형식 오류 |
| COMMON_003 | 서버 오류 |

---

## Authentication

| Code | Description |
|------|-------------|
| AUTH_001 | 로그인 실패 |
| AUTH_002 | 인증 필요 |
| AUTH_003 | 권한 없음 |

---

## Member

| Code | Description |
|------|-------------|
| MEMBER_001 | 로그인 아이디 중복 |
| MEMBER_002 | 비밀번호 불일치 |

---

# 12. Related Documents

| Document | Description |
|----------|-------------|
| planning.md | 프로젝트 목표 |
| feature-list.md | 기능 목록 |
| domain-model.md | 도메인 모델 |
| erd.md | 데이터베이스 설계 |
| wireframe.md | 화면 설계 |

---

# 13. Summary

GarageCare의 API는 RESTful 설계 원칙을 기반으로 하며, 일관된 URL 구조, Response 형식, Error Code 정책을 따른다.

본 문서는 Authentication과 Member API를 정의하며, 이후 Vehicle, Reservation, MaintenanceItem, Notice API의 상세 명세를 이어서 작성한다.

---

# 14. Vehicle API

Vehicle API는 고객이 자신의 차량을 등록하고 관리하기 위한 기능을 제공한다.

예약은 반드시 차량을 기반으로 생성되므로, Vehicle은 Reservation의 선행 조건이 되는 핵심 도메인이다.

관련 화면

- 내 차량 목록
- 차량 등록
- 차량 상세
- 차량 수정
- 예약 신청

관련 Domain

- Member
- Vehicle
- Reservation

---

# 15. Vehicle API Design Principles

Vehicle API는 다음 원칙을 따른다.

## Ownership

모든 차량은 반드시 하나의 Member에게 소속된다.

다른 회원의 차량은 조회하거나 수정할 수 없다.

---

## Soft Ownership

차량 삭제 기능은 MVP에서 제공하지 않는다.

예약 이력과 연결되기 때문이다.

필요 시 향후

```
active = false
```

방식으로 비활성화한다.

---

## Reservation Dependency

예약 생성 시

```
등록 차량 존재
```

가 선행 조건이다.

등록 차량이 없다면 예약을 생성할 수 없다.

---

## Security

Vehicle ID만 알고 있다고 접근할 수 없어야 한다.

반드시

```
vehicle.owner == loginMember
```

검증을 수행한다.

---

# 16. Vehicle API Summary

| Method | URL | Description |
|---------|-----|-------------|
| GET | /vehicles | 내 차량 목록 |
| POST | /vehicles | 차량 등록 |
| GET | /vehicles/{vehicleId} | 차량 상세 |
| PUT | /vehicles/{vehicleId} | 차량 수정 |

---

# 17. GET /vehicles

## Purpose

현재 로그인한 회원이 등록한 차량 목록을 조회한다.

---

## Authorization

```
CUSTOMER
```

---

## URL

```
GET /vehicles
```

---

## Request

Path Variable 없음

Body 없음

---

## Business Rules

조회 대상은

```
로그인한 회원
```

의 차량만 포함한다.

관리자는 별도 관리자 기능에서 조회한다.

차량은

```
등록 순
```

또는

```
최근 수정 순
```

으로 정렬할 수 있다.

MVP에서는 등록 순으로 조회한다.

---

## Success

HTTP

```
200 OK
```

---

## Response

```json
{
  "success": true,
  "data": [
    {
      "vehicleId": 1,
      "manufacturer": "현대",
      "model": "그랜저",
      "vehicleNumber": "12가3456",
      "modelYear": 2023,
      "mileage": 35000
    },
    {
      "vehicleId": 2,
      "manufacturer": "기아",
      "model": "쏘렌토",
      "vehicleNumber": "34나5678",
      "modelYear": 2022,
      "mileage": 42000
    }
  ],
  "message": "조회 성공"
}
```

---

## Empty Response

등록 차량이 없는 경우

```json
{
  "success": true,
  "data": [],
  "message": "등록된 차량이 없습니다."
}
```

---

## Error

```
401 Unauthorized
```

로그인하지 않은 사용자

```
403 Forbidden
```

권한 없음

---

## Related Screen

```
내 차량 목록
예약 신청
```

---

# 18. POST /vehicles

## Purpose

새로운 차량을 등록한다.

---

## Authorization

```
CUSTOMER
```

---

## URL

```
POST /vehicles
```

---

## Request

```json
{
  "manufacturer": "현대",
  "model": "그랜저",
  "vehicleNumber": "12가3456",
  "modelYear": 2023,
  "mileage": 35000
}
```

---

## Request Fields

| Field | Type | Required | Description |
|---------|------|----------|-------------|
| manufacturer | String | ✅ | 제조사 |
| model | String | ✅ | 모델명 |
| vehicleNumber | String | ✅ | 차량 번호 |
| modelYear | Integer | ❌ | 연식 |
| mileage | Integer | ❌ | 주행거리 |

---

# 19. Validation Rule

## manufacturer

- Required
- Blank 불가
- 최대 30자

---

## model

- Required
- Blank 불가
- 최대 50자

---

## vehicleNumber

- Required
- 차량 번호 형식
- 동일 회원 중복 불가

예)

```
12가3456
```

---

## modelYear

Optional

허용 범위

```
1980
~

현재년도 +1
```

---

## mileage

Optional

```
0 이상
```

---

# 20. Business Rules

차량 등록 시 다음 순서로 처리한다.

① 로그인 사용자 확인

↓

② 입력값 Validation

↓

③ 차량번호 중복 검사

↓

④ Vehicle 생성

↓

⑤ Member와 연결

↓

⑥ 저장

---

동일 회원이

```
12가3456
```

을 두 번 등록할 수 없다.

다른 회원은 동일 번호 등록 가능 여부는 운영 정책에 따라 결정한다.

MVP에서는

```
전체 시스템에서 차량번호 중복 불가
```

를 적용한다.

---

## Success

HTTP

```
201 Created
```

---

## Response

```json
{
  "success": true,
  "data": {
    "vehicleId": 3
  },
  "message": "차량이 등록되었습니다."
}
```

---

## Error Example

차량번호 중복

```json
{
  "success": false,
  "code": "VEHICLE_001",
  "message": "이미 등록된 차량번호입니다."
}
```

---

Validation 실패

```json
{
  "success": false,
  "code": "COMMON_001",
  "message": "입력값을 확인해 주세요."
}
```

---

## HTTP Status

| Status | Description |
|---------|-------------|
| 201 | 등록 성공 |
| 400 | Validation 실패 |
| 401 | 로그인 필요 |
| 409 | 차량번호 중복 |

---

## Related Screen

```
차량 등록
예약 신청
```

---

## Related Domain

```
Member
Vehicle
```

---

# 21. GET /vehicles/{vehicleId}

## Purpose

회원이 등록한 특정 차량의 상세 정보를 조회한다.

차량 상세 화면 및 차량 수정 화면에서 사용한다.

---

## Authorization

```
CUSTOMER
```

---

## URL

```
GET /vehicles/{vehicleId}
```

---

## Path Variable

| Name | Type | Description |
|------|------|-------------|
| vehicleId | Long | 차량 ID |

---

## Request

Body 없음

---

## Business Rules

조회 대상 차량은 반드시

```
현재 로그인한 회원의 차량
```

이어야 한다.

다른 회원의 차량을 조회할 수 없다.

관리자는 별도 관리자 API를 사용한다.

---

## Success

HTTP

```
200 OK
```

---

## Response

```json
{
  "success": true,
  "data": {
    "vehicleId": 1,
    "manufacturer": "현대",
    "model": "그랜저",
    "vehicleNumber": "12가3456",
    "modelYear": 2023,
    "mileage": 35200,
    "createdAt": "2026-07-20T13:40:20",
    "updatedAt": "2026-07-22T18:15:30"
  },
  "message": "조회 성공"
}
```

---

## Error

차량이 존재하지 않는 경우

```json
{
  "success": false,
  "code": "VEHICLE_002",
  "message": "차량을 찾을 수 없습니다."
}
```

---

본인 차량이 아닌 경우

```json
{
  "success": false,
  "code": "AUTH_003",
  "message": "접근 권한이 없습니다."
}
```

---

## HTTP Status

| Status | Description |
|---------|-------------|
| 200 | 조회 성공 |
| 401 | 로그인 필요 |
| 403 | 접근 권한 없음 |
| 404 | 차량 없음 |

---

## Related Screen

- 차량 상세
- 차량 수정

---

# 22. PUT /vehicles/{vehicleId}

## Purpose

등록된 차량 정보를 수정한다.

MVP에서는 다음 정보만 수정 가능하다.

- 제조사
- 모델명
- 연식
- 주행거리

차량번호는 수정하지 않는다.

---

## Authorization

```
CUSTOMER
```

---

## URL

```
PUT /vehicles/{vehicleId}
```

---

## Request

```json
{
    "manufacturer":"현대",
    "model":"그랜저",
    "modelYear":2024,
    "mileage":42000
}
```

---

## Request Fields

| Field | Required | Description |
|---------|----------|-------------|
| manufacturer | ✅ | 제조사 |
| model | ✅ | 모델 |
| modelYear | ❌ | 연식 |
| mileage | ❌ | 주행거리 |

---

## Validation

manufacturer

- Required
- 최대 30자

model

- Required
- 최대 50자

modelYear

```
1980 ~ 현재년도 +1
```

mileage

```
0 이상
```

---

## Business Rules

① 로그인 확인

↓

② 차량 존재 확인

↓

③ 소유자 확인

↓

④ Validation

↓

⑤ 수정

↓

⑥ 저장

---

예약 이력은 영향을 받지 않는다.

차량번호는 변경하지 않는다.

---

## Success

HTTP

```
200 OK
```

Response

```json
{
    "success":true,
    "data":{
        "vehicleId":1
    },
    "message":"차량 정보가 수정되었습니다."
}
```

---

## Error

```json
{
    "success":false,
    "code":"VEHICLE_002",
    "message":"차량을 찾을 수 없습니다."
}
```

```json
{
    "success":false,
    "code":"AUTH_003",
    "message":"접근 권한이 없습니다."
}
```

---

## HTTP Status

| Status | Meaning |
|---------|----------|
|200|수정 성공|
|400|Validation 실패|
|401|로그인 필요|
|403|권한 없음|
|404|차량 없음|

---

## Related Screen

- 차량 수정

---

# 23. Vehicle Authorization Policy

| Action | CUSTOMER | ADMIN |
|----------|:--------:|:-----:|
| 차량 등록 | ✅ | ❌ |
| 내 차량 조회 | ✅ | ❌ |
| 차량 상세 | ✅ | ❌ |
| 차량 수정 | ✅ | ❌ |
| 전체 차량 조회 | ❌ | 추후 지원 |

---

# 24. Vehicle Error Code

| Code | Description |
|------|-------------|
| VEHICLE_001 | 차량번호 중복 |
| VEHICLE_002 | 차량 없음 |
| VEHICLE_003 | 차량 소유자가 아님 |
| VEHICLE_004 | 잘못된 차량번호 형식 |
| VEHICLE_005 | 허용되지 않는 연식 |

---

# 25. Vehicle Sequence

## 차량 등록

```text
Member

↓

VehicleController

↓

VehicleService

↓

Member 확인

↓

Validation

↓

중복 검사

↓

Vehicle 생성

↓

Repository 저장

↓

Response
```

---

## 차량 조회

```text
Member

↓

VehicleController

↓

VehicleService

↓

회원 확인

↓

Vehicle 조회

↓

소유자 확인

↓

Response
```

---

## 차량 수정

```text
Member

↓

VehicleController

↓

VehicleService

↓

회원 확인

↓

Vehicle 조회

↓

소유자 확인

↓

Validation

↓

수정

↓

저장
```

---

# 26. Design Decisions

## 차량번호 수정 금지

차량번호는 차량을 식별하는 핵심 정보이다.

예약 이력 및 향후 정비 이력과 연결되므로 MVP에서는 수정 기능을 제공하지 않는다.

필요한 경우 차량을 새로 등록하는 방식으로 처리한다.

---

## 차량 삭제 미지원

예약 데이터 보존을 위해 삭제 기능을 제공하지 않는다.

향후

```
active = false
```

전략으로 변경 가능하다.

---

## 차량번호 중복 정책

현재는

```
전체 시스템에서 중복 불가
```

를 적용한다.

향후 다중 정비소를 지원할 경우

```
정비소별 중복 허용
```

으로 변경할 수 있다.

---

# 27. Open Decisions

향후 검토가 필요한 사항

- 대표 차량(Default Vehicle) 지원 여부
- 차량 이미지 업로드
- VIN(차대번호) 관리
- 제조사 자동 선택
- 모델 자동완성
- 차량 삭제 정책
- 차량번호 변경 정책

---

# 28. Summary

Vehicle API는 회원이 자신의 차량을 등록하고 관리하기 위한 기능을 제공한다.

모든 차량은 하나의 회원에게 소속되며, 예약 생성의 선행 조건으로 사용된다.

차량 삭제와 차량번호 변경은 데이터 무결성과 예약 이력 보존을 위해 MVP 범위에서 제외하며, 향후 서비스 확장 시 Soft Delete 및 관리 기능을 통해 지원할 수 있다.

---

# 29. Reservation API

Reservation API는 고객이 차량 정비 예약을 생성하고 관리하기 위한 기능을 제공한다.

GarageCare MVP의 핵심 기능이며, 예약 생성부터 완료까지의 전체 생명주기를 관리한다.

예약은 하나 이상의 정비 항목(ReservationItem)을 포함하며, 차량과 회원을 기준으로 생성된다.

관련 Domain

- Member
- Vehicle
- Reservation
- ReservationItem
- MaintenanceItem

관련 화면

- 예약 신청
- 예약 완료
- 예약 목록
- 예약 상세
- 관리자 예약 관리

---

# 30. Reservation API Design Principles

Reservation API는 다음 원칙을 따른다.

---

## Reservation Aggregate

Reservation은 Aggregate Root이며 ReservationItem을 포함한다.

```
Reservation
 ├── Vehicle
 ├── Member
 └── ReservationItem
        └── MaintenanceItem
```

ReservationItem은 Reservation 외부에서 직접 생성하거나 수정하지 않는다.

---

## Reservation Status

예약은 반드시 아래 상태 흐름을 따른다.

```
REQUESTED

↓

CONFIRMED

↓

COMPLETED
```

또는

```
REQUESTED

↓

CANCELED
```

완료된 예약은 다시 REQUESTED 상태로 변경하지 않는다.

---

## Ownership

예약은 생성한 회원만 조회 및 취소할 수 있다.

관리자는 모든 예약을 조회할 수 있다.

---

## ReservationItem Rule

예약에는 최소 하나 이상의 정비 항목이 포함되어야 한다.

```
ReservationItem.size() >= 1
```

동일한 정비 항목을 두 번 선택할 수 없다.

---

## Vehicle Ownership

예약하려는 차량은 반드시 로그인한 회원의 차량이어야 한다.

타인의 차량으로 예약할 수 없다.

---

# 31. Reservation API Summary

| Method | URL | Description |
|---------|-----|-------------|
| POST | /reservations | 예약 생성 |
| GET | /reservations | 내 예약 목록 |
| GET | /reservations/{reservationId} | 예약 상세 |
| PATCH | /reservations/{reservationId}/cancel | 예약 취소 |
| GET | /admin/reservations | 관리자 예약 조회 |
| PATCH | /admin/reservations/{reservationId}/status | 예약 상태 변경 |

---

# 32. POST /reservations

## Purpose

새로운 정비 예약을 생성한다.

예약 생성 시 차량과 정비 항목을 함께 선택한다.

---

## Authorization

```
CUSTOMER
```

---

## URL

```
POST /reservations
```

---

## Request

```json
{
  "vehicleId": 3,
  "reservationDate": "2026-08-15",
  "reservationTime": "10:30",
  "memo": "브레이크 소음 확인 부탁드립니다.",
  "maintenanceItems": [
    1,
    3,
    5
  ]
}
```

---

## Request Fields

| Field | Type | Required | Description |
|------|------|:--------:|-------------|
| vehicleId | Long | ✅ | 예약 차량 |
| reservationDate | Date | ✅ | 예약 날짜 |
| reservationTime | Time | ✅ | 예약 시간 |
| memo | String | ❌ | 요청사항 |
| maintenanceItems | List<Long> | ✅ | 정비 항목 ID 목록 |

---

# 33. Validation Rule

## vehicleId

- Required
- 존재하는 차량이어야 한다.
- 로그인 회원의 차량이어야 한다.

---

## reservationDate

- Required
- 오늘 이전 날짜 불가

예)

```
2026-08-15
```

가능

```
2026-07-10
```

불가

---

## reservationTime

- Required

예약 가능 시간만 선택 가능

예)

```
09:00

09:30

10:00

...

17:30
```

운영 정책 변경 시 Service에서 관리한다.

---

## memo

최대

```
300자
```

---

## maintenanceItems

필수

최소

```
1개 이상
```

동일한 항목 중복 선택 불가

비활성 항목 선택 불가

---

# 34. Business Rules

예약 생성은 다음 순서로 수행한다.

① 로그인 확인

↓

② 차량 존재 확인

↓

③ 차량 소유자 확인

↓

④ 입력값 Validation

↓

⑤ 예약 가능 날짜 확인

↓

⑥ 정비 항목 존재 확인

↓

⑦ 중복 정비 항목 제거 확인

↓

⑧ Reservation 생성

↓

⑨ ReservationItem 생성

↓

⑩ 저장

↓

⑪ 예약 완료

---

예약 생성 후

Status는

```
REQUESTED
```

로 저장된다.

---

예약 생성 시

ReservationItem은 MaintenanceItem 개수만큼 자동 생성된다.

예)

```
엔진오일

타이어

브레이크
```

선택

↓

```
Reservation

↓

ReservationItem

ReservationItem

ReservationItem
```

---

## Success

HTTP

```
201 Created
```

---

## Response

```json
{
  "success": true,
  "data": {
    "reservationId": 21,
    "status": "REQUESTED"
  },
  "message": "예약이 완료되었습니다."
}
```

---

## Error Example

차량 없음

```json
{
  "success": false,
  "code": "RESERVATION_001",
  "message": "차량을 찾을 수 없습니다."
}
```

---

정비 항목 없음

```json
{
  "success": false,
  "code": "RESERVATION_002",
  "message": "정비 항목을 선택해 주세요."
}
```

---

예약 가능 시간이 아님

```json
{
  "success": false,
  "code": "RESERVATION_003",
  "message": "예약 가능한 시간이 아닙니다."
}
```

---

타인의 차량

```json
{
  "success": false,
  "code": "RESERVATION_004",
  "message": "예약 가능한 차량이 아닙니다."
}
```

---

Validation

```json
{
  "success": false,
  "code": "COMMON_001",
  "message": "입력값을 확인해 주세요."
}
```

---

## HTTP Status

| Status | Description |
|---------|-------------|
|201|예약 생성|
|400|Validation 실패|
|401|로그인 필요|
|403|권한 없음|
|404|차량 또는 정비항목 없음|
|409|예약 불가|

---

## Related Screen

- 예약 신청
- 예약 완료

---

## Related Domain

- Reservation
- ReservationItem
- MaintenanceItem
- Vehicle
- Member

---

# 35. GET /reservations

## Purpose

현재 로그인한 회원의 예약 목록을 조회한다.

예약 내역 화면에서 사용하며, 예약 상태와 예약 일정을 확인할 수 있다.

---

## Authorization

```
CUSTOMER
```

---

## URL

```
GET /reservations
```

---

## Query Parameter

| Name | Type | Required | Description |
|------|------|----------|-------------|
| page | Integer | ❌ | 페이지 번호 (기본 0) |
| size | Integer | ❌ | 페이지 크기 (기본 10) |
| status | String | ❌ | 예약 상태 |
| sort | String | ❌ | 정렬 기준 |

---

## Example Request

```
GET /reservations?page=0&size=10
```

```
GET /reservations?status=REQUESTED
```

---

## Business Rules

조회 대상은

```
현재 로그인한 회원의 예약
```

만 포함한다.

관리자 예약은 조회되지 않는다.

예약은 기본적으로

```
예약일 내림차순
```

으로 조회한다.

가장 최근 예약이 가장 먼저 나타난다.

---

## Reservation Status

조회 가능한 상태

```
REQUESTED
```

```
CONFIRMED
```

```
COMPLETED
```

```
CANCELED
```

---

## Success

HTTP

```
200 OK
```

---

## Response

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "reservationId": 31,
        "vehicleModel": "그랜저",
        "reservationDate": "2026-08-15",
        "reservationTime": "10:30",
        "status": "REQUESTED"
      },
      {
        "reservationId": 29,
        "vehicleModel": "그랜저",
        "reservationDate": "2026-08-05",
        "reservationTime": "14:00",
        "status": "COMPLETED"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 2,
    "totalPages": 1
  },
  "message": "조회 성공"
}
```

---

## Empty Response

예약이 없는 경우

```json
{
  "success": true,
  "data": {
    "content": [],
    "page": 0,
    "size": 10,
    "totalElements": 0,
    "totalPages": 0
  },
  "message": "예약 내역이 없습니다."
}
```

---

## HTTP Status

| Status | Description |
|---------|-------------|
|200|조회 성공|
|401|로그인 필요|

---

## Related Screen

- 예약 목록

---

# 36. GET /reservations/{reservationId}

## Purpose

예약 상세 정보를 조회한다.

예약 화면에서 신청한 정비 항목과 요청사항을 확인하기 위해 사용한다.

---

## Authorization

```
CUSTOMER
```

---

## URL

```
GET /reservations/{reservationId}
```

---

## Path Variable

| Name | Type |
|------|------|
| reservationId | Long |

---

## Business Rules

예약은

```
예약한 회원
```

만 조회할 수 있다.

관리자는 관리자 API를 사용한다.

---

## Success

HTTP

```
200 OK
```

---

## Response

```json
{
  "success": true,
  "data": {
    "reservationId": 31,
    "vehicle": {
      "vehicleId": 3,
      "manufacturer": "현대",
      "model": "그랜저",
      "vehicleNumber": "12가3456"
    },
    "reservationDate": "2026-08-15",
    "reservationTime": "10:30",
    "status": "REQUESTED",
    "memo": "브레이크 소음 확인 부탁드립니다.",
    "maintenanceItems": [
      {
        "id": 1,
        "name": "엔진오일 교환"
      },
      {
        "id": 5,
        "name": "브레이크 점검"
      }
    ],
    "createdAt": "2026-07-25T12:10:22",
    "updatedAt": "2026-07-25T12:10:22"
  },
  "message": "조회 성공"
}
```

---

## Error

예약 없음

```json
{
  "success": false,
  "code": "RESERVATION_005",
  "message": "예약을 찾을 수 없습니다."
}
```

---

타인의 예약

```json
{
  "success": false,
  "code": "AUTH_003",
  "message": "접근 권한이 없습니다."
}
```

---

## HTTP Status

| Status | Description |
|---------|-------------|
|200|조회 성공|
|401|로그인 필요|
|403|권한 없음|
|404|예약 없음|

---

## Related Screen

- 예약 상세

---

# 37. Reservation Response Policy

예약 조회 시 응답은 항상 다음 원칙을 따른다.

---

## Vehicle

예약 당시 차량 정보를 반환한다.

```
vehicle
```

객체 안에 포함한다.

---

## Maintenance Item

정비 항목은

```
ReservationItem
```

을 기준으로 조회한다.

응답에서는 사용자가 이해하기 쉽도록

```
MaintenanceItem 목록
```

형태로 반환한다.

---

## Status

예약 상태는 Enum 문자열을 그대로 반환한다.

예)

```
REQUESTED
```

```
CONFIRMED
```

```
COMPLETED
```

```
CANCELED
```

클라이언트에서 상태에 따라 UI를 표시한다.

---

## Date Format

```
yyyy-MM-dd
```

---

## Time Format

```
HH:mm
```

---

## DateTime

```
yyyy-MM-dd'T'HH:mm:ss
```

---

# 38. Pagination Policy

예약 목록은 Spring Data Page를 사용한다.

---

## Default

```
page = 0

size = 10
```

---

## Maximum Size

```
50
```

이상을 요청하면

```
50
```

으로 제한한다.

---

## Sort

기본

```
reservationDate DESC
reservationTime DESC
```

---

향후 지원 예정

```
status

createdAt

updatedAt
```

---

# 39. Reservation Search Policy

현재 MVP에서는

```
status
```

검색만 지원한다.

예)

```
GET /reservations?status=COMPLETED
```

향후 추가 예정

- 날짜 범위 검색
- 차량 검색
- 정비 항목 검색
- 키워드 검색

---

# 40. Reservation Authorization Policy

| Action | CUSTOMER | ADMIN |
|---------|:--------:|:-----:|
| 예약 생성 | ✅ | ❌ |
| 내 예약 조회 | ✅ | ❌ |
| 예약 상세 | ✅ | ❌ |
| 예약 취소 | ✅ | ❌ |
| 전체 예약 조회 | ❌ | ✅ |
| 예약 상태 변경 | ❌ | ✅ |

---

# 41. Summary

Reservation 조회 API는 회원이 자신의 예약 정보를 안전하게 조회하기 위한 기능을 제공한다.

모든 조회는 로그인한 회원의 예약만 반환하며, 차량 정보와 정비 항목을 함께 제공하여 예약 상세 화면에서 필요한 모든 데이터를 한 번의 요청으로 확인할 수 있도록 설계하였다.

---

# 42. PATCH /reservations/{reservationId}/cancel

## Purpose

회원이 자신의 예약을 취소한다.

예약 취소는 데이터를 삭제하지 않고 상태(Status)를 `CANCELED`로 변경한다.

---

## Authorization

```
CUSTOMER
```

---

## URL

```
PATCH /reservations/{reservationId}/cancel
```

---

## Path Variable

| Name | Type | Description |
|------|------|-------------|
| reservationId | Long | 예약 ID |

---

## Request

Body 없음

---

## Business Rules

예약은 다음 조건을 만족할 경우에만 취소할 수 있다.

- 본인의 예약
- REQUESTED 상태
- CONFIRMED 상태

다음 상태에서는 취소할 수 없다.

```
COMPLETED
```

```
CANCELED
```

---

## Success

HTTP

```
200 OK
```

```json
{
  "success": true,
  "data": {
    "reservationId": 31,
    "status": "CANCELED"
  },
  "message": "예약이 취소되었습니다."
}
```

---

## Error

```json
{
  "success": false,
  "code": "RESERVATION_006",
  "message": "취소할 수 없는 예약입니다."
}
```

---

## HTTP Status

| Status | Description |
|---------|-------------|
|200|취소 성공|
|401|로그인 필요|
|403|권한 없음|
|404|예약 없음|
|409|취소 불가|

---

# 43. GET /admin/reservations

## Purpose

관리자가 전체 예약을 조회한다.

예약 관리 화면에서 사용한다.

---

## Authorization

```
ADMIN
```

---

## URL

```
GET /admin/reservations
```

---

## Query Parameter

| Name | Description |
|------|-------------|
| page | 페이지 |
| size | 페이지 크기 |
| status | 예약 상태 |
| reservationDate | 예약 날짜 |

---

## Business Rules

관리자는 모든 회원의 예약을 조회할 수 있다.

기본 정렬

```
reservationDate ASC
reservationTime ASC
```

오늘 예약이 먼저 표시된다.

---

## Success

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "reservationId": 31,
        "customerName": "박현우",
        "vehicleNumber": "12가3456",
        "reservationDate": "2026-08-15",
        "reservationTime": "10:30",
        "status": "REQUESTED"
      }
    ]
  }
}
```

---

# 44. PATCH /admin/reservations/{reservationId}/status

## Purpose

관리자가 예약 상태를 변경한다.

---

## Authorization

```
ADMIN
```

---

## URL

```
PATCH /admin/reservations/{reservationId}/status
```

---

## Request

```json
{
    "status":"CONFIRMED"
}
```

---

## Allowed Status

```
CONFIRMED
```

```
COMPLETED
```

```
CANCELED
```

---

## Business Rules

관리자는 다음 상태 전이만 수행할 수 있다.

```
REQUESTED

↓

CONFIRMED
```

```
CONFIRMED

↓

COMPLETED
```

```
REQUESTED

↓

CANCELED
```

```
CONFIRMED

↓

CANCELED
```

---

다음 변경은 허용하지 않는다.

```
COMPLETED

↓

REQUESTED
```

```
COMPLETED

↓

CONFIRMED
```

```
CANCELED

↓

REQUESTED
```

---

## Success

```json
{
  "success": true,
  "data": {
    "reservationId": 31,
    "status": "CONFIRMED"
  },
  "message": "예약 상태가 변경되었습니다."
}
```

---

# 45. Reservation Status Transition

예약 상태는 다음 생명주기를 따른다.

```text
REQUESTED
     │
     ▼
CONFIRMED
     │
     ▼
COMPLETED

REQUESTED
     │
     ▼
CANCELED

CONFIRMED
     │
     ▼
CANCELED
```

---

## State Policy

| Current | Next |
|----------|------|
| REQUESTED | CONFIRMED |
| REQUESTED | CANCELED |
| CONFIRMED | COMPLETED |
| CONFIRMED | CANCELED |

허용되지 않는 상태 변경은 모두 예외를 발생시킨다.

---

# 46. Reservation Error Code

| Code | Description |
|------|-------------|
| RESERVATION_001 | 차량을 찾을 수 없음 |
| RESERVATION_002 | 정비 항목 없음 |
| RESERVATION_003 | 예약 가능한 시간이 아님 |
| RESERVATION_004 | 차량 소유자가 아님 |
| RESERVATION_005 | 예약을 찾을 수 없음 |
| RESERVATION_006 | 취소할 수 없는 예약 |
| RESERVATION_007 | 잘못된 예약 상태 변경 |
| RESERVATION_008 | 예약 시간 중복 |
| RESERVATION_009 | 비활성 정비 항목 |

---

# 47. Reservation Sequence

## 예약 생성

```text
Member

↓

ReservationController

↓

ReservationService

↓

Vehicle 조회

↓

소유자 확인

↓

Validation

↓

MaintenanceItem 조회

↓

Reservation 생성

↓

ReservationItem 생성

↓

Repository 저장

↓

Response
```

---

## 예약 취소

```text
Member

↓

ReservationController

↓

ReservationService

↓

Reservation 조회

↓

소유자 확인

↓

취소 가능 여부 확인

↓

Status 변경

↓

저장

↓

Response
```

---

## 관리자 상태 변경

```text
Admin

↓

ReservationController

↓

ReservationService

↓

Reservation 조회

↓

상태 전이 검증

↓

Status 변경

↓

저장

↓

Response
```

---

# 48. Design Decisions

## ReservationItem 직접 수정 금지

ReservationItem은 Reservation Aggregate 내부 객체이다.

Controller에서 ReservationItem을 직접 수정하지 않는다.

---

## 예약 삭제 미지원

예약은 서비스 이력으로 활용된다.

삭제 대신

```
CANCELED
```

상태를 사용한다.

---

## 상태 변경은 Service에서만 수행

Controller에서 상태를 직접 변경하지 않는다.

모든 상태 변경은

```
Reservation.changeStatus()
```

또는

```
ReservationService
```

를 통해 수행한다.

---

## 예약 시간 중복 정책

동일 차량이 동일 날짜 및 시간에 중복 예약되는 것은 허용하지 않는다.

예약 생성 시 중복 여부를 검사하며, 중복일 경우 `409 Conflict`와 `RESERVATION_008` 오류를 반환한다.

---

# 49. Open Decisions

향후 검토 항목

- 예약 수정 기능
- 예약 재신청
- 예약 승인 알림
- 예약 대기열
- 정비 예상 소요시간
- 정비사 배정
- 캘린더 기반 예약 UI
- 공휴일 예약 정책

---

# 50. Summary

Reservation API는 GarageCare의 핵심 기능으로, 예약 생성부터 조회, 취소, 관리자 상태 관리까지 전체 예약 생명주기를 정의한다.

모든 예약은 `Reservation` Aggregate를 중심으로 관리되며, `ReservationItem`을 통해 하나 이상의 정비 항목을 포함한다.

예약 데이터는 삭제하지 않고 상태를 변경하여 이력을 보존하며, 상태 전이 규칙과 권한 정책을 통해 데이터의 무결성과 비즈니스 규칙을 유지한다.


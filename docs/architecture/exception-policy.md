# GarageCare Exception Policy

> Version: 1.0.0
> Status: Draft
> Last Updated: 2026-07-28

← [Docs](../README.md) / [Architecture](README.md)

---

# Overview

Exception Policy는 GarageCare에서 발생하는 예외를 일관된 방식으로 처리하기 위한 기준을 정의합니다.

예외 처리는 단순히 오류를 막는 것이 아니라, 사용자에게 적절한 응답을 제공하고 시스템의 안정성과 유지보수성을 높이는 중요한 요소입니다.

GarageCare는 모든 예외를 공통 예외 처리기(Global Exception Handler)를 통해 관리하며, 일관된 응답 형식을 제공합니다.

---

# Goals

Exception Policy의 목표는 다음과 같습니다.

- 예외 처리 방식을 일관되게 유지한다.
- 사용자에게 명확한 오류 정보를 제공한다.
- 비즈니스 예외와 시스템 예외를 구분한다.
- Controller 내부의 중복된 예외 처리 코드를 제거한다.
- 유지보수와 확장이 쉬운 구조를 만든다.

---

# Policy

GarageCare의 예외 처리 흐름은 다음과 같습니다.

```text
Client

↓

Controller

↓

Service

↓

Exception 발생

↓

Global Exception Handler

↓

ErrorResponse

↓

Client
```

모든 예외는 Global Exception Handler를 통해 처리하는 것을 원칙으로 합니다.

---

# Rules

## Controller

Controller에서는 예외를 직접 처리하지 않습니다.

Service에서 발생한 예외를 그대로 전달합니다.

---

## Service

Service는 비즈니스 규칙 위반 시 적절한 Business Exception을 발생시킵니다.

예시

- MemberNotFoundException
- DuplicateReservationException
- MaintenanceItemInactiveException

---

## Repository

Repository에서는 데이터 접근 예외만 발생할 수 있습니다.

데이터 접근 예외는 Spring이 제공하는 예외로 변환됩니다.

---

## Global Exception Handler

모든 예외는 Global Exception Handler에서 처리합니다.

Responsibilities

- HTTP Status 결정
- ErrorResponse 생성
- 로그 기록
- 사용자에게 오류 응답 반환

---

# Exception Categories

GarageCare는 예외를 다음과 같이 분류합니다.

| Category | Description | Example |
|----------|-------------|---------|
| Validation Exception | 입력값 오류 | @NotBlank |
| Business Exception | 업무 규칙 위반 | DuplicateReservationException |
| Resource Exception | 데이터 없음 | MemberNotFoundException |
| Security Exception | 인증·인가 실패 | AccessDeniedException |
| System Exception | 서버 내부 오류 | NullPointerException |

---

# Error Response

모든 예외는 동일한 응답 형식을 사용합니다.

예시

```json
{
  "timestamp": "2026-07-28T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "code": "RESERVATION_DUPLICATED",
  "message": "이미 예약된 시간입니다.",
  "path": "/reservations"
}
```

응답 형식을 통일하여 클라이언트가 일관성 있게 처리할 수 있도록 합니다.

---

# HTTP Status Policy

| Status | Description |
|---------|-------------|
| 400 Bad Request | 잘못된 입력 |
| 401 Unauthorized | 인증 실패 |
| 403 Forbidden | 권한 부족 |
| 404 Not Found | 리소스 없음 |
| 409 Conflict | 중복 데이터 |
| 500 Internal Server Error | 서버 오류 |

적절한 HTTP 상태 코드를 사용하여 오류의 원인을 명확하게 전달합니다.

---

# Examples

## Example 1

회원 조회

```text
회원 없음

↓

MemberNotFoundException

↓

404 Not Found
```

---

## Example 2

예약 생성

```text
예약 시간 중복

↓

DuplicateReservationException

↓

409 Conflict
```

---

## Example 3

회원가입

```text
이메일 형식 오류

↓

MethodArgumentNotValidException

↓

400 Bad Request
```

---

## Example 4

예상하지 못한 오류

```text
NullPointerException

↓

Global Exception Handler

↓

500 Internal Server Error
```

---

# Exception Flow

```text
Client

↓

Controller

↓

Service

↓

Business Exception

↓

Global Exception Handler

↓

ErrorResponse

↓

Client
```

모든 예외는 동일한 흐름을 따릅니다.

---

# Design Principles

GarageCare는 다음 원칙을 따릅니다.

- Controller에서 try-catch를 사용하지 않는다.
- Service에서 필요한 Business Exception을 발생시킨다.
- Repository는 데이터 접근만 담당한다.
- 모든 예외는 Global Exception Handler에서 처리한다.
- ErrorResponse 형식을 통일한다.
- HTTP Status는 의미에 맞게 사용한다.
- 사용자에게 내부 구현 정보를 노출하지 않는다.
- 예외 발생 시 반드시 로그를 기록한다.

---

# Future Extension

향후 API가 확장되거나 AI 상담 기능, 외부 서비스 연동 기능이 추가되더라도 동일한 예외 처리 정책을 유지합니다.

필요한 경우 Error Code를 추가하여 클라이언트가 오류를 더욱 세분화하여 처리할 수 있도록 확장할 예정입니다.

---

# Related Documents

- Overview
- Request Flow
- Validation Policy
- Transaction Policy
- Logging Policy
- Security Policy
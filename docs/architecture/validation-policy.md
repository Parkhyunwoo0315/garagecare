# GarageCare Validation Policy

> Version: 1.0.0
> Status: Draft
> Last Updated: 2026-07-28

← [Docs](../README.md) / [Architecture](README.md)

---

# Overview

Validation Policy는 GarageCare에서 사용자 입력과 도메인 규칙을 검증하는 기준을 정의합니다.

입력값 검증은 시스템의 안정성과 데이터 무결성을 유지하는 중요한 요소입니다. GarageCare는 **입력 형식 검증(Input Validation)**과 **비즈니스 규칙 검증(Business Validation)**을 명확히 분리하여 각 계층이 자신의 책임만 수행하도록 설계합니다.

---

# Goals

Validation Policy의 목표는 다음과 같습니다.

- 잘못된 요청을 가능한 한 빠르게 차단한다.
- 입력 형식 검증과 비즈니스 검증을 분리한다.
- 중복된 검증 로직을 최소화한다.
- 모든 요청에 대해 일관된 검증 기준을 제공한다.
- 유지보수와 확장이 쉬운 구조를 유지한다.

---

# Policy

GarageCare는 검증을 다음 두 단계로 수행합니다.

```text
Client

↓

Controller
(Input Validation)

↓

Service
(Business Validation)

↓

Repository
```

Controller는 요청 형식을 검증하고,

Service는 실제 업무 규칙을 검증합니다.

---

# Rules

## Controller Validation

Controller는 **요청 데이터 자체가 올바른 형식인지**를 확인합니다.

### Responsibilities

- 필수 값 여부
- 문자열 길이
- 이메일 형식
- 날짜 형식
- 숫자 범위
- Bean Validation 수행

비즈니스 로직은 포함하지 않습니다.

---

### Example

회원가입 요청

```java
@NotBlank
private String name;

@Email
private String email;

@Size(max = 20)
private String phone;
```

Bean Validation을 이용하여 형식을 검증합니다.

---

## Service Validation

Service는 **업무 규칙을 검증**합니다.

### Responsibilities

- 회원 존재 여부
- 차량 소유 여부
- 예약 가능 여부
- 중복 예약 여부
- 정비 항목 활성화 여부
- 예약 시간 충돌 여부

비즈니스 규칙은 반드시 Service에서 처리합니다.

---

### Example

예약 생성

```text
예약 생성

↓

회원 존재 여부 확인

↓

차량 존재 여부 확인

↓

예약 시간 중복 확인

↓

정비 항목 활성 여부 확인

↓

예약 생성
```

---

## Repository Validation

Repository는 검증을 수행하지 않습니다.

Repository의 책임은 데이터 저장과 조회입니다.

검증 로직을 포함하지 않습니다.

---

# Validation Scope

GarageCare에서 수행하는 대표적인 검증입니다.

| Category | Example |
|----------|---------|
| Required Value | 이름 누락 |
| Format | 이메일 형식 |
| Length | 전화번호 길이 |
| Range | 예약 가능 시간 |
| Duplicate | 동일 시간 예약 |
| Existence | 회원 존재 여부 |
| Ownership | 차량 소유 여부 |
| Business Rule | 비활성 정비 항목 예약 |

---

# Validation Flow

입력 검증은 다음 순서로 진행됩니다.

```text
Client

↓

Request DTO

↓

Bean Validation

↓

Controller

↓

Service Validation

↓

Repository
```

Controller에서 실패하면 Service는 호출되지 않습니다.

---

# Validation Failure

검증에 실패하면 즉시 요청 처리를 중단합니다.

```text
Client

↓

Controller

↓

Validation Error

↓

Global Exception Handler

↓

400 Bad Request
```

비즈니스 규칙 검증 실패는 적절한 Business Exception을 발생시킵니다.

---

# Examples

## Example 1

회원가입

입력값

```text
이름 = ""
```

결과

```text
@NotBlank 실패

↓

400 Bad Request
```

---

## Example 2

예약 생성

입력값은 정상

↓

회원 조회 실패

↓

MemberNotFoundException

↓

예약 실패

---

## Example 3

예약 생성

회원 존재

↓

차량 존재

↓

예약 시간 중복

↓

DuplicateReservationException

↓

예약 실패

---

# Design Principles

GarageCare는 다음 원칙을 따릅니다.

- 입력 형식 검증은 Controller에서 수행한다.
- 비즈니스 검증은 Service에서 수행한다.
- Repository는 검증하지 않는다.
- Bean Validation을 적극 활용한다.
- Entity는 스스로 검증하지 않는다.
- 검증 실패 시 즉시 요청을 종료한다.
- 검증 로직은 중복 작성하지 않는다.
- 모든 검증 실패는 일관된 예외 응답으로 반환한다.

---

# Future Extension

향후 AI 상담, 알림 기능, 외부 API 연동 등이 추가되더라도 Validation Policy는 동일한 원칙을 유지합니다.

새로운 기능은 기존 검증 체계를 확장하는 방식으로 구현하며, Controller와 Service의 책임은 변경하지 않습니다.

---

# Related Documents

- Overview
- Layered Architecture
- Request Flow
- Transaction Policy
- Exception Policy
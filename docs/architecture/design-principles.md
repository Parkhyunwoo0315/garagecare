# GarageCare Design Principles

> Version: 1.0.0
> Status: Draft
> Last Updated: 2026-07-28

← [Docs](../README.md) / [Architecture](README.md)

---

# Overview

Design Principles는 GarageCare를 설계하면서 일관되게 적용하는 핵심 원칙을 정의합니다.

GarageCare는 단순히 기능을 구현하는 프로젝트가 아니라, 실제 서비스 개발 환경에서 사용되는 설계 방식을 학습하고 적용하는 것을 목표로 합니다.

모든 기능은 이 문서에서 정의한 원칙을 기반으로 설계하고 구현합니다.

---

# Goals

Design Principles의 목표는 다음과 같습니다.

- 일관된 아키텍처를 유지한다.
- 유지보수가 쉬운 구조를 만든다.
- 기능 추가가 쉬운 구조를 설계한다.
- 각 계층의 책임을 명확히 분리한다.
- 실제 서비스 개발 방식에 가까운 구조를 지향한다.

---

# Principles

## 1. Single Responsibility Principle

각 클래스는 하나의 책임만 가집니다.

예시

- Controller는 요청을 처리합니다.
- Service는 비즈니스 로직을 수행합니다.
- Repository는 데이터 접근을 담당합니다.

하나의 클래스가 여러 역할을 수행하지 않습니다.

---

## 2. Separation of Concerns

각 계층은 자신의 책임만 수행합니다.

```text
Controller

↓

Service

↓

Repository

↓

Database
```

각 계층은 다른 계층의 역할을 대신하지 않습니다.

---

## 3. Dependency Direction

의존성은 항상 상위 계층에서 하위 계층으로만 흐릅니다.

```text
Controller

↓

Service

↓

Repository
```

Repository가 Service를 참조하거나, Service가 Controller를 참조하지 않습니다.

---

## 4. Domain-Oriented Structure

GarageCare는 기능 중심(Package by Feature) 구조를 사용합니다.

```text
member

reservation

vehicle

notice
```

관련된 클래스는 같은 패키지에 배치하여 응집도를 높입니다.

---

## 5. Explicit Responsibilities

모든 계층은 명확한 역할을 가집니다.

| Layer | Responsibility |
|--------|----------------|
| Controller | 요청 및 응답 |
| Service | 비즈니스 로직 |
| Repository | 데이터 접근 |
| Domain | 비즈니스 상태와 규칙 |

책임이 겹치지 않도록 설계합니다.

---

## 6. Consistency

동일한 문제는 항상 동일한 방식으로 해결합니다.

예시

- 예외 처리 방식 통일
- Validation 방식 통일
- Transaction 관리 통일
- Logging 방식 통일

프로젝트 전체에서 일관성을 유지합니다.

---

## 7. Fail Fast

잘못된 요청은 가능한 한 빠르게 차단합니다.

```text
Client

↓

Validation

↓

Error

↓

Request End
```

불필요한 비즈니스 로직 수행을 방지합니다.

---

## 8. Keep It Simple

복잡한 구조보다 이해하기 쉬운 구조를 우선합니다.

현재 MVP 단계에서는 과도한 추상화나 불필요한 패턴을 적용하지 않습니다.

필요성이 생길 때 구조를 확장합니다.

---

## 9. Extensibility

기존 코드를 크게 수정하지 않고 기능을 추가할 수 있도록 설계합니다.

예시

- AI 상담
- 엔진오일 알림
- OAuth 로그인
- 이메일 알림
- SMS 알림

확장은 기존 구조를 유지하는 방향으로 진행합니다.

---

## 10. Readability

코드는 작성보다 읽는 시간이 훨씬 많습니다.

GarageCare는 가독성을 가장 중요한 품질 중 하나로 생각합니다.

이를 위해

- 명확한 클래스 이름
- 의미 있는 메서드 이름
- 일관된 패키지 구조
- 충분한 문서화

를 유지합니다.

---

# Examples

## Reservation Feature

예약 기능은 다음 원칙을 따릅니다.

```text
ReservationController

↓

ReservationService

↓

ReservationRepository

↓

Database
```

각 계층은 자신의 책임만 수행합니다.

---

## Validation

```text
Controller

↓

Input Validation

↓

Service

↓

Business Validation
```

입력 검증과 비즈니스 검증을 분리합니다.

---

## Exception

```text
Service

↓

Business Exception

↓

Global Exception Handler

↓

Client
```

모든 예외는 동일한 방식으로 처리합니다.

---

## Logging

```text
Business Logic

↓

Logger

↓

Log Output
```

로그는 필요한 정보만 기록합니다.

---

# Decision Guidelines

새로운 기능을 추가할 때는 다음 질문을 기준으로 설계합니다.

- 이 기능의 책임은 무엇인가?
- 어느 계층에 구현하는 것이 적절한가?
- 기존 구조를 유지할 수 있는가?
- 다른 기능과 일관성을 유지하는가?
- 확장성을 해치지 않는가?
- 불필요한 복잡성을 만들지는 않는가?

모든 구현은 이러한 기준을 만족하도록 설계합니다.

---

# Future Extension

GarageCare는 향후 다음 기능을 고려하여 설계합니다.

- Spring Security
- JWT Authentication
- AI Maintenance Recommendation
- Notification Service
- Email Service
- SMS Service
- Dashboard
- External API Integration

새로운 기능이 추가되더라도 현재 설계 원칙을 유지하며 자연스럽게 확장할 수 있도록 합니다.

---

# Related Documents

- Overview
- Layered Architecture
- Package Structure
- Request Flow
- Transaction Policy
- Validation Policy
- Exception Policy
- Security Policy
- Logging Policy
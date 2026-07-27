# GarageCare Layered Architecture

> Version: 1.0.0  
> Status: Draft  
> Last Updated: 2026-07-28

← [Docs](../README.md) / [Architecture](README.md)

---

# Overview

GarageCare는 Spring Boot의 권장 구조인 **Layered Architecture**를 기반으로 설계되었습니다.

각 계층은 하나의 책임만 수행하며, 상위 계층은 바로 아래 계층에만 의존합니다.

이를 통해 높은 응집도(High Cohesion)와 낮은 결합도(Low Coupling)를 유지하며 유지보수성과 확장성을 확보합니다.

---

# Architecture Diagram

```text
                 Client
                    │
                    ▼
      ┌─────────────────────────┐
      │     Controller Layer    │
      └─────────────────────────┘
                    │
                    ▼
      ┌─────────────────────────┐
      │      Service Layer      │
      └─────────────────────────┘
                    │
                    ▼
      ┌─────────────────────────┐
      │    Repository Layer     │
      └─────────────────────────┘
                    │
                    ▼
      ┌─────────────────────────┐
      │       Database          │
      └─────────────────────────┘

              ▲
              │
      Domain(Entity)
```

---

# Dependency Rule

GarageCare는 아래와 같은 의존 방향을 따릅니다.

```text
Controller
    │
    ▼
Service
    │
    ▼
Repository
    │
    ▼
Database
```

Entity는 모든 계층에서 사용할 수 있지만, 비즈니스 로직은 Service 계층에서만 수행합니다.

---

# Layer Responsibilities

## Controller Layer

Controller는 클라이언트의 요청과 응답을 담당합니다.

### Responsibilities

- HTTP 요청 수신
- DTO 검증
- Service 호출
- View 또는 API 응답 반환

### Must Not

- 비즈니스 로직 작성
- 데이터베이스 접근
- Entity 직접 반환

---

## Service Layer

Service는 GarageCare의 핵심 비즈니스 로직을 담당합니다.

### Responsibilities

- 예약 생성
- 예약 취소
- 회원 관리
- 차량 관리
- 트랜잭션 관리
- 도메인 검증

### Must Not

- HTTP 처리
- View 처리
- SQL 작성

---

## Repository Layer

Repository는 데이터 저장소와의 통신만 담당합니다.

### Responsibilities

- CRUD 수행
- Query 실행
- Entity 저장 및 조회

### Must Not

- 비즈니스 로직 작성
- 트랜잭션 관리
- DTO 반환

---

## Domain Layer

Domain은 프로젝트의 핵심 비즈니스 데이터를 표현합니다.

### Responsibilities

- Entity 정의
- 도메인 상태 관리
- 도메인 규칙 표현

### Must Not

- Controller 의존
- Repository 의존
- View 처리

---

# Layer Communication

계층 간 호출은 아래 방향으로만 이루어집니다.

```text
Client

↓

Controller

↓

Service

↓

Repository

↓

Database
```

역방향 호출은 허용하지 않습니다.

예를 들어

Repository가 Service를 호출하거나

Controller가 Repository를 직접 호출해서는 안 됩니다.

---

# DTO Policy

계층 간 데이터 전달은 DTO를 사용합니다.

```text
Client

↓

Request DTO

↓

Controller

↓

Service

↓

Entity

↓

Repository

↓

Entity

↓

Service

↓

Response DTO

↓

Controller

↓

Client
```

Entity는 외부로 직접 노출하지 않습니다.

---

# Transaction Boundary

트랜잭션은 Service 계층에서 관리합니다.

```java
@Transactional
public ReservationResponse createReservation(...) {
    ...
}
```

읽기 전용 조회는 아래와 같이 작성합니다.

```java
@Transactional(readOnly = true)
```

Repository에서는 트랜잭션을 관리하지 않습니다.

---

# Design Rules

GarageCare는 다음 규칙을 따릅니다.

- Controller는 Service만 호출한다.
- Service는 Repository만 호출한다.
- Repository는 Database만 접근한다.
- Entity는 Controller로 반환하지 않는다.
- DTO를 통해 데이터를 전달한다.
- 비즈니스 로직은 Service에서 수행한다.
- Controller는 요청과 응답만 처리한다.

---

# Why Layered Architecture?

GarageCare는 실제 정비소 운영을 고려한 프로젝트이므로 기능이 지속적으로 추가될 가능성이 높습니다.

Layered Architecture를 적용하면 새로운 기능이 추가되더라도 기존 코드에 미치는 영향을 최소화할 수 있으며, 계층별 책임이 명확해져 유지보수성과 테스트 용이성을 확보할 수 있습니다.

또한 Spring Boot 생태계에서 가장 널리 사용되는 구조이기 때문에 학습과 실무 적용 측면에서도 높은 활용도를 기대할 수 있습니다.

---

# Related Documents

- Overview
- Package Structure
- Request Flow
- Transaction Policy
- Validation Policy
- Exception Policy
- Design Principles
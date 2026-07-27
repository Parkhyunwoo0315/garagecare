# GarageCare Request Flow

> Version: 1.0.0  
> Status: Draft  
> Last Updated: 2026-07-28

← [Docs](../README.md) / [Architecture](README.md)

---

# Overview

Request Flow는 클라이언트의 요청이 GarageCare 내부에서 어떻게 처리되고 응답으로 반환되는지를 정의합니다.

GarageCare는 Spring MVC 기반의 요청 처리 흐름을 따르며, 각 계층은 명확한 책임을 가지고 협력합니다.

모든 요청은 동일한 흐름을 따르며, 계층 간 직접적인 접근은 허용하지 않습니다.

---

# Request Lifecycle

모든 HTTP 요청은 아래 순서로 처리됩니다.

```text
Client
   │
   ▼
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
   │
   ▼
Repository
   │
   ▼
Service
   │
   ▼
Controller
   │
   ▼
Client
```

---

# Request Processing

## 1. Client

사용자가 브라우저 또는 API를 통해 요청을 전송합니다.

예시

- 예약 생성
- 예약 조회
- 회원가입
- 로그인

---

## 2. Controller

Controller는 HTTP 요청을 수신합니다.

### Responsibilities

- URL 매핑
- Request DTO 생성
- 입력값 검증
- Service 호출
- Response DTO 반환

Controller에서는 비즈니스 로직을 수행하지 않습니다.

---

## 3. Service

Service는 비즈니스 로직을 수행합니다.

예를 들어 예약 생성 요청에서는 다음과 같은 작업이 수행됩니다.

- 회원 존재 여부 확인
- 차량 존재 여부 확인
- 정비 항목 검증
- 예약 생성
- ReservationItem 생성
- Repository 저장 요청

모든 비즈니스 규칙은 Service에서 처리합니다.

---

## 4. Repository

Repository는 데이터 저장소와 통신합니다.

### Responsibilities

- Entity 저장
- Entity 조회
- Query 실행

비즈니스 로직은 포함하지 않습니다.

---

## 5. Database

Repository가 요청한 데이터를 저장하거나 조회합니다.

GarageCare에서는 개발 단계에서는 H2 Database를 사용하고, 운영 환경에서는 MySQL을 사용할 수 있도록 설계합니다.

---

# Response Flow

조회 또는 저장이 완료되면 결과는 역순으로 반환됩니다.

```text
Database

↓

Repository

↓

Service

↓

Controller

↓

Client
```

Entity는 Service에서 필요한 형태로 가공된 후 Response DTO로 변환됩니다.

---

# Example Flow

예약 생성 요청의 처리 과정입니다.

```text
POST /reservations

↓

ReservationController

↓

ReservationService

↓

MemberRepository

↓

VehicleRepository

↓

MaintenanceRepository

↓

ReservationRepository

↓

Database

↓

ReservationResponse

↓

Client
```

---

# DTO Conversion

GarageCare는 Entity를 외부에 직접 노출하지 않습니다.

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

Database
```

응답은 다음과 같이 처리됩니다.

```text
Database

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

---

# Validation Flow

입력값 검증은 두 단계로 수행합니다.

## Controller Validation

기본적인 입력 형식을 검증합니다.

예시

- Null 여부
- 길이 제한
- 이메일 형식
- 날짜 형식

Bean Validation을 사용합니다.

---

## Service Validation

도메인 규칙을 검증합니다.

예시

- 회원 존재 여부
- 차량 소유 여부
- 예약 가능 여부
- 중복 예약 여부
- 비활성 정비 항목 여부

---

# Exception Flow

요청 처리 중 예외가 발생하면 아래와 같이 처리합니다.

```text
Client

↓

Controller

↓

Service

↓

Exception

↓

GlobalExceptionHandler

↓

ErrorResponse

↓

Client
```

모든 예외는 공통 예외 처리기를 통해 일관된 형식으로 반환합니다.

---

# Transaction Flow

데이터 변경 요청은 Service 계층에서 트랜잭션을 시작합니다.

```text
Controller

↓

@Transactional

↓

Service

↓

Repository

↓

Database
```

정상적으로 완료되면 Commit,

예외가 발생하면 Rollback 됩니다.

---

# Design Rules

GarageCare는 다음 규칙을 따릅니다.

- 모든 요청은 Controller에서 시작한다.
- Controller는 Service만 호출한다.
- Service는 Repository만 호출한다.
- Repository는 Database만 접근한다.
- Entity는 외부로 직접 반환하지 않는다.
- Request DTO와 Response DTO를 분리한다.
- 비즈니스 로직은 Service에서만 수행한다.
- 트랜잭션은 Service에서 관리한다.
- 예외는 Global Exception Handler에서 처리한다.

---

# Future Extension

향후 AI 상담, 알림 서비스, 외부 API 연동 등의 기능이 추가되더라도 동일한 Request Flow를 유지합니다.

새로운 기능은 기존 요청 처리 구조를 변경하지 않고 Service 계층에서 확장하는 것을 원칙으로 합니다.

---

# Related Documents

- Overview
- Layered Architecture
- Package Structure
- Transaction Policy
- Validation Policy
- Exception Policy
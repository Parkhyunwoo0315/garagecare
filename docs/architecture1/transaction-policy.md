# GarageCare Transaction Policy

> Version: 1.0.0
> Status: Draft
> Last Updated: 2026-07-28

← [Docs](../README.md) / [Architecture](README.md)

---

# Overview

Transaction Policy는 GarageCare에서 데이터의 일관성과 무결성을 보장하기 위한 트랜잭션 관리 원칙을 정의합니다.

GarageCare는 Spring Framework의 선언적 트랜잭션(Declarative Transaction Management)을 사용하며, 모든 데이터 변경 작업은 Service 계층에서 트랜잭션을 관리합니다.

---

# Goals

Transaction Policy의 목표는 다음과 같습니다.

- 데이터 무결성 보장
- 원자성(Atomicity) 보장
- 예외 발생 시 자동 Rollback
- Service 계층에서 일관된 트랜잭션 관리
- 유지보수성과 확장성 향상

---

# Transaction Boundary

GarageCare에서는 트랜잭션의 시작과 종료를 Service 계층에서 관리합니다.

```text
Controller

↓

@Service

↓

@Transactional

↓

Business Logic

↓

Repository

↓

Database
```

Controller와 Repository는 트랜잭션을 시작하지 않습니다.

---

# Why Service Layer?

비즈니스 로직은 하나 이상의 Repository를 사용할 수 있습니다.

예를 들어 예약 생성은 다음과 같은 작업을 하나의 트랜잭션으로 처리해야 합니다.

- 회원 조회
- 차량 조회
- 정비 항목 조회
- 예약 생성
- ReservationItem 생성
- 예약 저장

위 작업 중 하나라도 실패하면 전체 작업이 취소되어야 합니다.

따라서 트랜잭션은 Service 계층에서 관리합니다.

---

# Read Transaction

조회 기능은 읽기 전용 트랜잭션을 사용합니다.

```java
@Transactional(readOnly = true)
```

적용 대상

- 예약 조회
- 회원 조회
- 차량 조회
- 공지사항 조회

읽기 전용 트랜잭션을 사용하면 불필요한 변경 감지를 줄여 성능을 향상시킬 수 있습니다.

---

# Write Transaction

데이터를 변경하는 기능은 일반 트랜잭션을 사용합니다.

```java
@Transactional
```

적용 대상

- 회원가입
- 예약 생성
- 예약 취소
- 공지사항 등록
- 차량 등록

트랜잭션이 정상적으로 종료되면 Commit 됩니다.

---

# Rollback Policy

다음 상황에서는 Rollback을 수행합니다.

- RuntimeException 발생
- IllegalArgumentException 발생
- Business Exception 발생

Rollback 이후에는 어떠한 데이터도 저장되지 않습니다.

---

# Reservation Example

예약 생성은 하나의 트랜잭션으로 처리됩니다.

```text
ReservationService

↓

회원 조회

↓

차량 조회

↓

정비 항목 조회

↓

예약 생성

↓

ReservationItem 생성

↓

저장

↓

Commit
```

중간에 예외가 발생하면

```text
ReservationService

↓

Exception

↓

Rollback
```

전체 작업이 취소됩니다.

---

# Multiple Repository

하나의 Service에서는 여러 Repository를 사용할 수 있습니다.

예시

```text
ReservationService

├── MemberRepository
├── VehicleRepository
├── MaintenanceRepository
└── ReservationRepository
```

모든 작업은 하나의 트랜잭션 안에서 수행됩니다.

---

# Transaction Rules

GarageCare는 다음 규칙을 따릅니다.

## Controller

트랜잭션을 생성하지 않습니다.

---

## Service

트랜잭션을 시작합니다.

비즈니스 로직을 수행합니다.

---

## Repository

데이터 접근만 수행합니다.

트랜잭션을 관리하지 않습니다.

---

# Lazy Loading

Entity의 연관관계는 기본적으로 Lazy Loading을 사용합니다.

```java
fetch = FetchType.LAZY
```

필요한 경우에만 데이터를 조회하여 성능을 향상시킵니다.

---

# Nested Transaction

GarageCare에서는 중첩 트랜잭션을 사용하지 않습니다.

하나의 요청은 하나의 Service에서 하나의 트랜잭션으로 처리하는 것을 원칙으로 합니다.

---

# Long Transaction

긴 트랜잭션은 지양합니다.

Service에서는

- 외부 API 호출
- 이메일 전송
- 파일 업로드

등의 시간이 오래 걸리는 작업을 트랜잭션 내부에서 수행하지 않습니다.

필요한 경우 트랜잭션 종료 후 처리합니다.

---

# Design Principles

GarageCare는 다음 원칙을 따릅니다.

- 트랜잭션은 Service에서 시작한다.
- Controller는 트랜잭션을 생성하지 않는다.
- Repository는 트랜잭션을 생성하지 않는다.
- 조회는 readOnly 트랜잭션을 사용한다.
- 변경은 일반 트랜잭션을 사용한다.
- RuntimeException 발생 시 Rollback 한다.
- 하나의 비즈니스 작업은 하나의 트랜잭션으로 처리한다.
- 긴 작업은 트랜잭션 밖에서 수행한다.

---

# Future Extension

향후 AI 상담, 문자 알림, 이메일 발송, 외부 API 연동 기능이 추가될 경우에도 트랜잭션 내부에서는 데이터 저장만 수행합니다.

시간이 오래 걸리는 작업은 이벤트 기반 또는 비동기 처리로 확장할 수 있도록 설계합니다.

---

# Related Documents

- Overview
- Layered Architecture
- Request Flow
- Validation Policy
- Exception Policy
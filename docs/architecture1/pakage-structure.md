# GarageCare Package Structure

> Version: 1.0.0  
> Status: Draft  
> Last Updated: 2026-07-28

← [Docs](../README.md) / [Architecture](README.md)

---

# Overview

GarageCare는 **Package by Domain** 방식을 채택합니다.

기능(도메인)을 기준으로 패키지를 구성하여 관련 클래스가 하나의 영역 안에서 함께 관리되도록 설계했습니다.

Spring Boot 프로젝트에서 흔히 사용하는 Layered Architecture와 결합하여, 각 도메인 내부에서는 Controller, Service, Repository, DTO가 하나의 기능 단위로 응집되도록 구성합니다.

---

# Why Package by Domain?

Package by Layer 방식은 프로젝트가 커질수록 하나의 패키지에 수십 개의 클래스가 모여 탐색이 어려워질 수 있습니다.

GarageCare는 기능 단위의 유지보수성과 확장성을 높이기 위해 Package by Domain 방식을 선택했습니다.

이 구조에서는 새로운 기능을 추가하거나 수정할 때 하나의 도메인 내부만 확인하면 되므로 개발 효율이 높아집니다.

---

# Package Structure

```
src
└── main
    └── java
        └── com.garagecare
            ├── global
            │
            ├── member
            ├── vehicle
            ├── reservation
            ├── maintenance
            ├── notice
            │
            └── GarageCareApplication.java
```

---

# Global Package

공통 기능을 관리하는 패키지입니다.

```
global
├── config
├── exception
├── security
├── dto
├── util
└── common
```

## Responsibilities

- 공통 설정
- 예외 처리
- Security 설정
- 공통 DTO
- 유틸리티
- 상수 및 공통 클래스

---

# Domain Package

각 기능은 독립적인 도메인으로 구성합니다.

예를 들어 Reservation 도메인은 아래 구조를 가집니다.

```
reservation

├── controller
├── service
├── repository
├── entity
├── dto
│   ├── request
│   └── response
└── exception
```

각 도메인은 동일한 구조를 유지합니다.

---

# Domain Packages

GarageCare에서는 다음 도메인을 사용합니다.

| Domain | Responsibility |
|---------|----------------|
| member | 회원 관리 |
| vehicle | 차량 관리 |
| reservation | 예약 관리 |
| maintenance | 정비 항목 관리 |
| notice | 공지사항 관리 |

---

# Package Responsibilities

## Controller

클라이언트 요청을 처리합니다.

- Request DTO 수신
- Response DTO 반환
- Service 호출

---

## Service

비즈니스 로직을 수행합니다.

- 예약 생성
- 예약 취소
- 회원 관리
- 차량 관리

---

## Repository

데이터 저장소와 통신합니다.

- Entity 저장
- Entity 조회
- Query 실행

---

## Entity

도메인의 핵심 데이터를 표현합니다.

- 상태 관리
- 연관관계 정의
- JPA 매핑

---

## DTO

계층 간 데이터를 전달합니다.

```
dto

request/

response/
```

Request와 Response를 분리하여 명확한 역할을 부여합니다.

---

# Package Dependency

패키지 간 의존 관계는 다음과 같습니다.

```
Controller

↓

Service

↓

Repository

↓

Entity

↓

Database
```

도메인 간 직접 의존은 최소화하며 필요한 경우 Service를 통해 협력합니다.

---

# Naming Convention

## Package

```
member
reservation
vehicle
maintenance
notice
```

모두 소문자를 사용합니다.

---

## Controller

```
MemberController
ReservationController
NoticeController
```

---

## Service

```
MemberService
ReservationService
VehicleService
```

---

## Repository

```
MemberRepository
ReservationRepository
```

---

## Entity

```
Member
Reservation
Vehicle
Notice
```

---

## DTO

```
CreateReservationRequest

ReservationResponse

UpdateMemberRequest

MemberResponse
```

Request와 Response를 명확하게 구분합니다.

---

# Design Rules

GarageCare는 다음 규칙을 따릅니다.

- 모든 도메인은 동일한 패키지 구조를 유지한다.
- DTO는 request와 response를 분리한다.
- 공통 기능은 global 패키지에서 관리한다.
- 도메인 간 직접 접근을 최소화한다.
- Entity는 entity 패키지에만 위치한다.
- Repository는 Entity만 관리한다.

---

# Future Expansion

기능이 추가될 경우 동일한 구조를 유지합니다.

예를 들어 AI 상담 기능이 추가된다면 다음과 같이 확장됩니다.

```
ai

├── controller
├── service
├── repository
├── entity
├── dto
└── exception
```

기존 구조를 변경하지 않고 새로운 도메인만 추가하여 확장할 수 있습니다.

---

# Related Documents

- Overview
- Layered Architecture
- Request Flow
- Design Principles
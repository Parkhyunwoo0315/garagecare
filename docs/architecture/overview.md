# GarageCare Architecture Overview

> Version: 1.0.0
> Status: Draft
> Last Updated: 2026-07-28

← [Docs](../README.md) / [Architecture](README.md)

---

# Overview

GarageCare는 자동차 정비소의 예약 및 고객 관리 업무를 디지털화하기 위한 웹 애플리케이션입니다.

전화 중심의 예약 방식에서 발생하는 예약 누락, 일정 관리의 어려움, 고객 정보 관리의 비효율성을 해결하기 위해 Spring Boot 기반의 웹 서비스를 구축하는 것을 목표로 합니다.

본 문서는 GarageCare의 전체 아키텍처와 설계 목표를 설명하며, 이후 문서에서 다루는 세부 설계의 기준이 됩니다.

---

# Architecture Goals

GarageCare의 아키텍처는 다음 목표를 달성하기 위해 설계되었습니다.

- 유지보수가 쉬운 구조
- 명확한 계층 분리
- 높은 응집도와 낮은 결합도
- 기능 확장이 용이한 구조
- 테스트 가능한 구조
- Spring Boot Best Practice 적용
- 실무에서 널리 사용하는 설계 방식 적용

---

# Architecture Style

GarageCare는 다음과 같은 아키텍처 스타일을 기반으로 구현됩니다.

| Style | Description |
|--------|-------------|
| Layered Architecture | 계층별 책임을 분리하여 유지보수성을 높입니다. |
| Package by Domain | 기능(도메인) 중심으로 패키지를 구성합니다. |
| MVC Pattern | Presentation Layer와 Business Logic을 분리합니다. |
| RESTful API | 클라이언트와 서버 간 일관된 통신 방식을 제공합니다. |

---

# High-Level Architecture

GarageCare는 Presentation Layer와 Business Layer를 분리하는 Layered Architecture를 채택합니다.

```text
            Client
               │
               ▼
        Presentation Layer
        (Controller / DTO)
               │
               ▼
        Application Layer
           (Service)
               │
               ▼
          Domain Layer
           (Entity)
               │
               ▼
      Persistence Layer
         (Repository)
               │
               ▼
            Database
```

각 계층은 자신의 책임만 수행하며, 상위 계층은 바로 아래 계층에만 의존합니다.

---

# Design Objectives

GarageCare는 다음과 같은 설계 원칙을 지향합니다.

## 1. Separation of Concerns

각 계층은 하나의 책임만 가집니다.

예를 들어 Controller는 요청을 처리하고, Service는 비즈니스 로직을 수행하며, Repository는 데이터 접근만 담당합니다.

---

## 2. High Cohesion

관련된 기능은 하나의 도메인으로 묶어 관리합니다.

예를 들어 예약과 관련된 객체는 Reservation 도메인 내부에서 관리됩니다.

---

## 3. Low Coupling

도메인 간 의존성을 최소화하여 기능 변경이 다른 영역에 미치는 영향을 줄입니다.

---

## 4. Extensibility

새로운 기능이 추가되더라도 기존 구조를 변경하지 않고 확장할 수 있도록 설계합니다.

예를 들어 AI 상담 기능이나 엔진오일 교체 알림 기능은 기존 예약 기능을 수정하지 않고 독립적으로 추가할 수 있습니다.

---

## 5. Maintainability

프로젝트 규모가 커져도 일관된 구조를 유지할 수 있도록 계층과 패키지를 명확히 구분합니다.

---

# Technology Stack

| Category | Technology |
|----------|------------|
| Language | Java 17 |
| Framework | Spring Boot |
| Build Tool | Gradle |
| Template Engine | Thymeleaf |
| Database | H2 (Development) |
| ORM | Spring Data JPA |
| Validation | Bean Validation |
| Security | Spring Security |
| Version Control | Git / GitHub |

---

# Why This Architecture?

GarageCare는 학습 프로젝트이면서 실제 정비소 운영을 고려한 포트폴리오 프로젝트입니다.

따라서 단순히 기능 구현에 집중하기보다, 실제 서비스 개발에서 사용하는 설계 방식을 경험하는 것을 중요한 목표로 삼았습니다.

이를 위해 Spring Boot에서 가장 널리 사용되는 Layered Architecture를 기반으로 프로젝트를 설계하고, 도메인 중심의 패키지 구조와 명확한 계층 분리를 적용하여 유지보수성과 확장성을 확보하고자 합니다.

---

# Related Documents

다음 문서를 통해 세부 설계를 확인할 수 있습니다.

- Package Structure
- Layered Architecture
- Request Flow
- Transaction Policy
- Validation Policy
- Exception Policy
- Security Policy
- Logging Policy
- Design Principles
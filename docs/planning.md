# GarageCare Planning

> Version: 1.0.0
>
> Status: Draft
>
> Last Updated: 2026-07-13

---

# 1. Overview

## 1.1 Project Name

GarageCare

---

## 1.2 Project Description

GarageCare는 소규모 자동차 정비소의 예약 및 고객 차량 관리 업무를 지원하기 위한 웹서비스이다.

고객은 원하는 시간에 온라인으로 예약을 신청하고 자신의 차량 정보를 관리할 수 있으며,

관리자는 예약 현황과 고객 차량 정보를 하나의 시스템에서 효율적으로 관리할 수 있다.

본 프로젝트는 단순한 예약 기능 구현이 아닌,

실제 자동차 정비소에서 발생하는 반복 업무와 관리의 비효율을 개선하는 것을 목표로 한다.

---

## 1.3 Vision

> 누구나 전화 없이 쉽게 예약하고,
>
> 관리자는 예약보다 정비에 집중할 수 있는 서비스를 만든다.

GarageCare는 예약 시스템을 만드는 프로젝트가 아니다.

정비소가 고객과 소통하는 방식을 개선하고,

관리자가 반복적인 예약 관리 업무보다

정비 자체에 더 집중할 수 있도록 돕는 운영 플랫폼을 지향한다.

---

# 2. Background

## Why GarageCare?

프로젝트를 시작하게 된 가장 큰 이유는

실제 자동차 정비소를 운영하는 환경을 가까이에서 경험했기 때문이다.

현재 대부분의 소규모 자동차 정비소에서는

전화 또는 방문 예약을 중심으로 업무가 이루어진다.

예약이 많은 시간에는

작업을 중단하고 전화를 받아야 하며,

고객 요청사항을 메모하거나 기억에 의존하는 경우도 많다.

이러한 방식은

예약 누락,

일정 중복,

고객 정보 관리의 어려움,

반복적인 상담 업무로 이어질 수 있다.

GarageCare는 이러한 문제를 해결하기 위해

예약과 고객 정보를 하나의 웹서비스에서 관리하는 것을 목표로 한다.

---

# 3. Problem Statement

GarageCare가 해결하려는 문제는 다음과 같다.

## Customer

고객은

- 정비 가능 시간을 미리 확인하기 어렵다.
- 전화 예약이 번거롭다.
- 예약 내용을 다시 확인하기 어렵다.
- 자신의 차량 정보를 체계적으로 관리하기 어렵다.

---

## Administrator

관리자는

- 작업 중에도 예약 전화를 받아야 한다.
- 예약 내용을 수기로 관리하는 경우가 많다.
- 예약 누락이 발생할 수 있다.
- 고객 차량 정보를 쉽게 조회하기 어렵다.
- 공지사항이나 운영시간 변경을 전달하기 어렵다.

---

# 4. Project Goal

GarageCare는

다음 네 가지 목표를 가진다.

## Goal 1

전화 중심 예약을

온라인 예약 시스템으로 전환한다.

---

## Goal 2

예약,

차량,

고객 정보를

하나의 시스템에서 관리한다.

---

## Goal 3

관리자의 예약 관리 시간을 줄여

정비 업무에 집중할 수 있도록 한다.

---

## Goal 4

향후

정비 이력,

교체주기 알림,

AI 상담 기능까지

확장 가능한 구조를 설계한다.

---

# 5. Core Value

GarageCare가 중요하게 생각하는 가치는

다음과 같다.

## Accessibility

전화 없이

언제 어디서든

예약할 수 있어야 한다.

---

## Simplicity

예약 과정은

가능한 적은 단계로 이루어져야 한다.

---

## Reliability

예약 정보는

누락되거나 중복되지 않아야 한다.

---

## Maintainability

기능 추가와 수정이

쉽도록 설계한다.

---

## Scalability

현재는

하나의 정비소를 대상으로 하지만

향후 다양한 기능을 추가할 수 있어야 한다.

---

# 6. Scope

## Included

GarageCare MVP에서 제공하는 기능

- 회원가입
- 로그인
- 차량 등록
- 예약 등록
- 예약 조회
- 예약 취소
- 공지사항 조회
- 관리자 예약 관리
- 관리자 공지사항 관리

---

## Excluded

MVP에서는 구현하지 않는 기능

- AI 상담
- 엔진오일 교체주기 알림
- 실시간 채팅
- 온라인 결제
- 통계 대시보드

위 기능들은

서비스 구조를 고려하여

향후 버전에서 구현한다.

---

# 7. Target Users

GarageCare는 두 명의 주요 사용자를 대상으로 한다.

서비스에서 제공하는 모든 기능은

각 사용자의 업무와 행동을 중심으로 설계한다.

---

## 7.1 Customer

자동차 정비를 위해

정비소를 방문하는 일반 고객

### Characteristics

- 전화보다 온라인 예약을 선호한다.
- 예약 가능한 시간을 미리 확인하고 싶어한다.
- 자신의 차량 정보를 한 번만 등록하고 반복 사용하기를 원한다.
- 예약 상태를 직접 확인하기를 원한다.

### Primary Goals

- 원하는 시간에 예약하기
- 예약 변경 및 취소하기
- 차량 정보 관리하기
- 정비소 공지사항 확인하기

### Pain Points

현재 대부분의 소규모 정비소에서는

예약을 위해 직접 전화를 해야 한다.

영업시간 외에는 예약이 어렵고,

예약 내용도 다시 확인하기 어렵다.

차량 정보 역시

예약할 때마다 반복적으로 전달해야 하는 경우가 많다.

---

## 7.2 Administrator

자동차 정비소 운영자

또는 예약을 관리하는 직원

### Characteristics

- 작업과 고객 응대를 동시에 수행한다.
- 예약 정보를 빠르게 확인해야 한다.
- 고객 차량 정보를 쉽게 조회해야 한다.
- 공지사항을 직접 관리한다.

### Primary Goals

- 예약 누락 방지
- 예약 일정 관리
- 고객 차량 정보 조회
- 공지사항 관리

### Pain Points

전화 예약은

정비 작업 중에도 응대해야 한다.

작업 흐름이 끊기며,

예약 내용이 메모에 의존하는 경우가 많다.

예약 변경이나 취소도

기존 예약과 함께 확인해야 하기 때문에

관리 비용이 증가한다.

---

# 8. User Requirements

GarageCare는

사용자의 행동을 기준으로 요구사항을 정의한다.

---

## 8.1 Customer Requirements

### Account

고객은

회원가입과 로그인을 할 수 있어야 한다.

---

### Vehicle

고객은

자신의 차량 정보를 등록할 수 있어야 한다.

등록된 차량은

예약 시 다시 사용할 수 있어야 한다.

---

### Reservation

고객은

예약 가능한 날짜를 선택할 수 있어야 한다.

예약 내용을 확인할 수 있어야 한다.

예약을 취소할 수 있어야 한다.

---

### Notice

고객은

정비소 운영시간,

공지사항,

휴무일 등을 확인할 수 있어야 한다.

---

## 8.2 Administrator Requirements

### Reservation Management

관리자는

전체 예약을 조회할 수 있어야 한다.

예약 상태를 변경할 수 있어야 한다.

예약을 검색할 수 있어야 한다.

---

### Customer Management

관리자는

예약과 연결된 고객 정보를 확인할 수 있어야 한다.

차량 정보를 함께 확인할 수 있어야 한다.

---

### Notice Management

관리자는

공지사항을 등록,

수정,

삭제할 수 있어야 한다.

---

# 9. Functional Requirements

GarageCare MVP는

다음 기능을 포함한다.

## Authentication

- 회원가입

- 로그인

- 로그아웃

---

## Vehicle

- 차량 등록

- 차량 조회

- 차량 수정

---

## Reservation

- 예약 등록

- 예약 조회

- 예약 취소

---

## Notice

- 공지사항 조회

---

## Admin

- 예약 조회

- 예약 상태 변경

- 공지사항 관리

---

# 10. Non-functional Requirements

GarageCare는

기능뿐 아니라

서비스 품질도 중요하게 고려한다.

---

## Usability

예약은

가능한 적은 단계로 완료되어야 한다.

사용자는

현재 예약 상태를 쉽게 확인할 수 있어야 한다.

---

## Security

비밀번호는

암호화하여 저장한다.

고객은

다른 고객의 예약을 조회할 수 없다.

관리자 기능은

권한이 있는 사용자만 접근할 수 있다.

---

## Reliability

예약 정보는

누락되지 않아야 한다.

예약 상태는

정의된 상태만 사용할 수 있다.

---

## Maintainability

Controller,

Service,

Repository의 책임을 분리한다.

도메인 중심으로

프로젝트를 구성한다.

---

## Scalability

현재는

하나의 정비소를 대상으로 한다.

향후

AI 상담,

정비 이력,

교체주기 알림,

통계 기능을

추가할 수 있도록 설계한다.

---

# 11. MVP Definition

GarageCare는

실제 정비소에서 사용할 수 있는 최소 기능(MVP)을 우선 구현한다.

MVP는

"기능을 많이 만드는 것"보다

"예약 업무를 실제로 수행할 수 있는 수준"을 목표로 한다.

---

## MVP Goal

고객이

전화 없이 예약을 완료하고,

관리자가

예약을 확인하고 관리할 수 있는 환경을 제공한다.

---

## MVP Scope

### Customer

- 회원가입
- 로그인
- 차량 등록
- 예약 등록
- 예약 조회
- 예약 취소
- 공지사항 조회

---

### Administrator

- 예약 조회
- 예약 상태 변경
- 공지사항 관리

---

## Out of Scope

다음 기능은

초기 버전에서 제외한다.

- AI 상담
- 정비주기 알림
- 이메일 알림
- 문자 알림
- 통계 Dashboard
- 온라인 결제

---

## Decision

MVP에서는

예약 프로세스의 완성도를 우선한다.

부가 기능은

예약 시스템이 안정적으로 동작한 이후 추가한다.

---

# 12. Business Rules

GarageCare는

다음 비즈니스 규칙을 따른다.

---

## Reservation

고객은

예약 가능한 날짜만 선택할 수 있다.

---

예약은

반드시 등록된 차량과 연결된다.

---

예약 상태는

다음 상태만 사용할 수 있다.

REQUESTED

↓

CONFIRMED

↓

COMPLETED

또는

↓

CANCELED

---

COMPLETED 상태의 예약은

고객이 취소할 수 없다.

---

관리자만

예약 상태를 변경할 수 있다.

---

## Vehicle

차량은

회원과 연결된다.

차량은

여러 번 예약에 사용할 수 있다.

---

## Notice

공지사항은

관리자만 등록할 수 있다.

고객은

조회만 가능하다.

---

# 13. Success Criteria

GarageCare MVP는

다음 조건을 만족하면 성공으로 판단한다.

---

## Customer

고객은

회원가입을 할 수 있다.

로그인을 할 수 있다.

차량을 등록할 수 있다.

예약을 등록할 수 있다.

예약을 조회할 수 있다.

예약을 취소할 수 있다.

---

## Administrator

관리자는

예약을 조회할 수 있다.

예약 상태를 변경할 수 있다.

공지사항을 관리할 수 있다.

---

## Technical

프로젝트는

예외 없이 실행된다.

주요 기능은

테스트 코드가 존재한다.

README를 통해

프로젝트를 이해할 수 있다.

---

## Real World

실제 정비소 운영자가

기본 예약 업무에 사용할 수 있다고 판단한다.

---

# 14. Future Roadmap

GarageCare는

MVP 이후

다음 기능을 순차적으로 추가한다.

---

Version 1.1

- 예약 알림

- 차량 정비 이력

---

Version 1.2

- 엔진오일 교체주기 알림

- 소모품 관리

---

Version 2.0

- AI 정비 상담

- 증상 기반 정비 추천

- 예약 분석 Dashboard

---

# 15. Development Principles

GarageCare는

다음 원칙을 따른다.

---

## Simplicity

복잡한 구조보다

이해하기 쉬운 구조를 우선한다.

---

## Readability

누구나 읽기 쉬운 코드를 작성한다.

---

## Consistency

프로젝트 전체에서

동일한 네이밍과 구조를 유지한다.

---

## Scalability

현재 기능보다

향후 확장을 고려하여 설계한다.

---

## Documentation

모든 중요한 의사결정은

문서로 기록한다.

---

# 16. Related Documents

GarageCare는

문서를 역할별로 분리하여 관리한다.

planning.md

↓

feature-list.md

↓

user-flow.md

↓

domain-model.md

↓

erd.md

↓

wireframe.md

↓

api-spec.md

↓

architecture.md

---

각 문서는

하나의 책임만 가진다.

중복 작성하지 않는다.

필요한 경우

Issue와 상호 참조한다.

---

# 17. Product Strategy

GarageCare는 "기능이 많은 서비스"보다 "실제로 사용할 수 있는 서비스"를 목표로 한다.

모든 기능은 실제 정비소 운영 과정에서 발생하는 문제를 해결하는지 여부를 기준으로 우선순위를 결정한다.

기능 추가보다 사용자 경험과 운영 효율을 우선한다.

---

## Product Principle

GarageCare는 다음 원칙을 따른다.

### Solve Real Problems

실제 정비소에서 발생하는 문제를 해결하는 기능만 개발한다.

단순히 구현이 가능한 기능이 아니라 실제 운영 과정에서 도움이 되는 기능을 우선한다.

---

### Build Small, Improve Fast

처음부터 완벽한 서비스를 만드는 대신,

작동하는 최소 기능(MVP)을 먼저 완성하고

실제 사용자의 피드백을 기반으로 개선한다.

---

### Data Before Automation

자동화보다 먼저

신뢰할 수 있는 데이터를 축적한다.

예약 데이터와 차량 정보가 충분히 쌓인 이후

AI 추천과 자동화 기능을 도입한다.

---

### Documentation First

중요한 설계 변경과 의사결정은

코드보다 먼저 문서에 기록한다.

문서는 구현을 위한 기준이며,

프로젝트의 의도를 설명하는 역할을 한다.

---

# 18. Decision Records

GarageCare에서 이루어진 주요 의사결정을 기록한다.

향후 요구사항 변경 시

변경 이유를 추적하기 위한 기준 문서로 사용한다.

---

## ADR-001

### Title

MVP에서는 예약 기능을 최우선으로 구현한다.

### Context

정비소 운영에서 가장 많은 시간을 차지하는 업무는

예약 접수와 예약 관리이다.

예약 기능이 안정적으로 동작하지 않으면

차량 관리나 AI 기능도 의미가 없다.

### Decision

회원가입,

차량 등록,

예약 등록,

예약 관리 기능을

MVP 핵심 기능으로 선정한다.

### Consequence

AI,

통계,

알림 기능은

초기 버전에서 제외한다.

---

## ADR-002

### Title

차량 정보는 회원과 분리하여 관리한다.

### Context

고객은

차량을 교체하거나

두 대 이상의 차량을 보유할 수 있다.

예약은 고객이 아니라

차량과 연결되는 것이 자연스럽다.

### Decision

Member

↓

Vehicle

↓

Reservation

구조를 사용한다.

### Consequence

예약 이력 관리와

차량별 정비 이력을

쉽게 확장할 수 있다.

---

## ADR-003

### Title

예약 상태는 고정된 상태값을 사용한다.

### Context

예약 상태를 자유롭게 입력하면

검색과 통계,

상태 관리가 어려워진다.

### Decision

REQUESTED

CONFIRMED

COMPLETED

CANCELED

네 가지 상태만 사용한다.

### Consequence

예약 흐름을 일관되게 관리할 수 있다.

---

## ADR-004

### Title

초기 버전은 단일 정비소를 대상으로 한다.

### Context

현재 프로젝트는

실제 소규모 정비소 운영을 기준으로 한다.

여러 정비소를 지원하면

권한,

매장,

재고,

통계 등

고려해야 할 요소가 급격히 증가한다.

### Decision

GarageCare v1은

단일 정비소만 지원한다.

### Consequence

복잡도를 줄이고

핵심 예약 경험에 집중한다.

향후 멀티 지점 지원은 별도 프로젝트로 확장한다.

---

# 19. Assumptions

프로젝트는 다음 가정을 기반으로 진행한다.

- 정비소는 하나이다.
- 관리자 계정은 최소 1명 존재한다.
- 고객은 회원가입 후 예약한다.
- 예약 가능한 시간은 관리자가 운영한다.
- 차량은 최소 1대 이상 등록 후 예약할 수 있다.
- 인터넷 연결이 가능한 환경에서 서비스를 사용한다.

---

# 20. Risks

예상되는 위험 요소를 정의한다.

| Risk | Impact | Mitigation |
|-------|--------|------------|
| 요구사항 변경 | High | 문서 우선 수정 후 구현 |
| 기능 범위 증가 | High | MVP 범위 유지 |
| 일정 지연 | Medium | 우선순위 기반 개발 |
| DB 구조 변경 | Medium | ERD 검토 후 반영 |
| 테스트 부족 | High | 핵심 기능 테스트 코드 작성 |

---

# 21. Success Metrics

프로젝트의 성공 여부는 다음 기준으로 판단한다.

### Product

- 고객이 예약을 완료할 수 있다.
- 관리자가 예약을 관리할 수 있다.
- 예약 정보가 누락되지 않는다.

### Engineering

- 프로젝트가 정상 실행된다.
- 핵심 기능에 테스트 코드가 존재한다.
- 계층 구조가 명확하게 분리되어 있다.

### Documentation

- README만으로 프로젝트를 이해할 수 있다.
- 설계 문서와 구현 내용이 일치한다.
- 주요 의사결정이 문서로 기록되어 있다.

### Real World

실제 정비소 운영자가

예약 업무에 활용 가능하다고 판단한다.

---

# 22. User Journey

GarageCare는

기능 중심이 아니라

사용자 경험(User Experience)을 중심으로 설계한다.

---

## Customer Journey

### Step 1

회원가입

↓

로그인

---

### Step 2

차량 등록

↓

내 차량 정보 저장

---

### Step 3

예약 페이지 이동

↓

예약 가능한 날짜 선택

↓

정비 항목 선택

↓

요청사항 입력

↓

예약 완료

---

### Step 4

예약 조회

↓

예약 상태 확인

↓

필요 시 예약 취소

---

### Step 5

정비 완료

↓

다음 방문 준비

---

## Administrator Journey

관리자는

예약 생성보다

예약 관리가 주요 업무이다.

---

예약 확인

↓

예약 승인

↓

예약 완료 처리

↓

공지사항 관리

↓

다음 예약 준비

---

# 23. Service Flow

GarageCare의 핵심 흐름은

예약을 중심으로 이루어진다.

Customer

↓

Login

↓

Vehicle

↓

Reservation

↓

Administrator

↓

Maintenance

↓

Completed

이 흐름은

모든 기능 설계의 기준이 된다.

---

# 24. MVP Release Strategy

GarageCare는

기능을 한 번에 모두 개발하지 않는다.

작은 단위로

점진적으로 개선한다.

---

## Phase 1

Planning

- 요구사항 정의
- 기능 정의
- ERD
- API

---

## Phase 2

Foundation

- 프로젝트 생성
- 기본 구조
- DB 연결

---

## Phase 3

Core Features

- 회원
- 차량
- 예약

---

## Phase 4

Admin

- 예약 관리
- 공지사항

---

## Phase 5

Quality

- 테스트
- 리팩토링
- README

---

## Phase 6

Release

v1.0.0

---

# 25. Version Roadmap

GarageCare는

버전별 목표를 명확하게 구분한다.

---

## Version 1.0

MVP

- 회원
- 차량
- 예약
- 공지사항

---

## Version 1.1

Customer Experience

- 예약 알림
- 차량 관리 개선

---

## Version 1.2

Maintenance

- 정비 이력
- 소모품 관리

---

## Version 2.0

AI

- AI 상담
- AI 정비 추천
- AI FAQ

---

## Version 3.0

Business

- Dashboard
- 통계
- 예약 분석

---

# 26. Documentation Policy

GarageCare는

모든 설계를 문서로 관리한다.

문서는

코드보다 먼저 작성한다.

---

## Principle

한 문서는

하나의 책임만 가진다.

중복 작성하지 않는다.

---

planning.md

↓

feature-list.md

↓

user-flow.md

↓

domain-model.md

↓

erd.md

↓

wireframe.md

↓

api-spec.md

↓

architecture.md

---

각 문서는

필요한 경우

Issue와 연결한다.

---

# 27. Git Workflow

GarageCare는

GitHub 중심으로 프로젝트를 관리한다.

---

Issue

↓

Branch

↓

Commit

↓

Pull Request

↓

Merge

↓

Close Issue

---

모든 작업은

Issue를 기준으로 진행한다.

---

Commit은

하나의 목적만 가진다.

---

모든 주요 변경은

문서에 기록한다.

---

# 28. Coding Principles

GarageCare는

다음 원칙을 따른다.

---

## Readability

코드는

누구나 읽기 쉬워야 한다.

---

## Responsibility

각 계층은

하나의 책임만 가진다.

---

## Consistency

네이밍과 구조는

프로젝트 전체에서 동일해야 한다.

---

## Testability

비즈니스 로직은

테스트할 수 있어야 한다.

---

## Extensibility

새로운 기능이

기존 구조를 크게 변경하지 않고

추가될 수 있어야 한다.

---

# 29. Project Philosophy

GarageCare는

예약 시스템을 만드는 프로젝트가 아니다.

GarageCare는

소규모 자동차 정비소의

운영 방식을

조금 더 효율적으로 만드는 프로젝트이다.

기술은 목적이 아니라

문제를 해결하기 위한 수단이다.

새로운 기능보다

사용자에게 실제로 도움이 되는 기능을 우선한다.

모든 설계와 구현은

실제 사용자를 기준으로 판단한다.

---

# 30. Final Statement

GarageCare는

Spring Boot를 학습하기 위해 시작한 프로젝트가 아니다.

실제 정비소에서 발생하는 문제를 해결하기 위해 시작한 프로젝트이다.

프로젝트는

기능의 개수가 아니라

문제를 얼마나 효과적으로 해결했는가로 평가받아야 한다.

GarageCare는

지속적인 개선을 통해

실제 운영 가능한 서비스로 발전시키는 것을 목표로 한다.
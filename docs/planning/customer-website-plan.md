# GarageCare
## Customer Website Product Requirements Document

| 항목 | 내용 |
|---|---|
| Project | GarageCare |
| Document | Customer Website PRD |
| Version | 0.1.0 |
| Status | Draft |
| Milestone | Sprint 6 — Customer Experience & Web Design |
| Product Type | 자동차 정비 예약 및 차량 관리 서비스 |
| Primary Users | 자동차 소유 고객, 정비소 관리자 |
| Technology | HTML5, CSS3, Thymeleaf, Spring Boot |
| Last Updated | 2026-10-27 |

---

# 1. Executive Summary

## 1.1 Product Overview

GarageCare는 자동차 소유자가 자신의 차량을 편리하게 관리하고, 필요한 정비 서비스를 예약할 수 있도록 지원하는 웹 기반 자동차 관리 서비스이다.

기존 소규모 자동차 정비소의 전화 중심 예약 방식과 분산된 고객 관리 문제를 개선하고, 고객과 정비소를 연결하는 통합적인 디지털 서비스 환경을 제공한다.

GarageCare는 단순히 정비 예약을 접수하는 시스템을 넘어, 고객이 자신의 차량 정보와 정비 예약 현황을 한곳에서 확인하고 지속적으로 관리받는 경험을 제공하는 것을 목표로 한다.

## 1.2 Product Vision

> **내 차를 가장 편리하게 관리하는 방법, GarageCare.**

고객이 자동차 관리에 필요한 정보를 쉽게 확인하고, 정비가 필요할 때 복잡한 과정 없이 서비스를 이용할 수 있는 환경을 구축한다.

장기적으로는 차량별 정비 이력과 관리 주기를 기반으로 고객에게 필요한 정보를 적절한 시점에 제공하는 개인화된 자동차 관리 서비스로 발전한다.

## 1.3 Product Mission

GarageCare의 핵심 목표는 다음과 같다.

1. 자동차 정비 예약 과정의 불편함을 줄인다.
2. 고객의 차량 및 예약 정보를 통합적으로 관리한다.
3. 정비소와 고객 사이의 정보 접근성을 개선한다.
4. 고객이 자신의 차량을 지속적으로 관리받는 경험을 제공한다.
5. 소규모 정비소의 디지털 운영 기반을 구축한다.

## 1.4 Core Value Proposition

| 핵심 가치 | 고객에게 제공하는 경험 |
|---|---|
| Convenience | 전화 없이 간편하게 정비를 예약한다. |
| Transparency | 내 차량과 예약 상태를 명확하게 확인한다. |
| Continuity | 차량 정보를 반복 입력하지 않고 지속적으로 관리한다. |
| Trust | 정비소 정보와 제공 서비스를 쉽게 확인한다. |
| Care | 차량 관리에 필요한 정보를 한곳에서 확인한다. |

---

# 2. Problem Definition

## 2.1 Background

소규모 자동차 정비소는 고객 예약과 문의를 전화 중심으로 처리하는 경우가 많다.

이러한 운영 방식은 고객과 정비소 모두에게 불편함을 발생시킬 수 있다.

고객은 정비소 영업시간에 맞춰 전화해야 하며, 예약 가능 여부와 정비 관련 정보를 확인하기 위해 별도의 문의가 필요하다.

정비소는 정비 작업 중에도 전화에 대응해야 하고, 예약 및 고객 정보를 별도로 관리해야 하는 부담이 발생한다.

GarageCare는 실제 소규모 정비소의 운영 환경을 바탕으로 이러한 문제를 해결하기 위해 기획되었다.

## 2.2 Customer Problems

### Problem 1. 예약 과정의 불편함

고객은 정비를 예약하기 위해 정비소에 직접 전화해야 하는 경우가 있다.

전화 연결이 어렵거나 영업시간이 아닐 경우 예약이 지연될 수 있다.

### Problem 2. 차량 정보의 분산

고객은 자신의 차량 정보를 반복적으로 전달해야 할 수 있다.

여러 차량을 보유한 경우 차량별 예약과 관리 정보를 구분하기도 번거롭다.

### Problem 3. 정보 접근성 부족

고객이 정비소의 위치, 영업시간, 제공 서비스 및 공지사항을 확인하기 어려울 수 있다.

이로 인해 단순 문의가 증가하고 서비스 이용 전 불확실성이 발생한다.

### Problem 4. 지속적인 차량 관리 경험 부족

정비 예약은 일회성으로 끝나기 쉽다.

고객이 이전 예약과 차량 관리 정보를 확인할 수 없다면 장기적인 차량 관리 서비스로 이어지기 어렵다.

## 2.3 Business Problems

정비소 관점에서는 다음과 같은 문제가 존재한다.

- 전화 문의 및 예약 응대에 따른 업무 부담
- 예약 정보의 분산 관리
- 고객 및 차량 정보 확인의 비효율성
- 정비소 공지사항 전달의 어려움
- 고객과의 지속적인 관계 형성 수단 부족

## 2.4 Problem Statement

> 자동차 소유 고객은 정비소 정보를 쉽게 확인하고, 자신의 차량을 등록하여 필요한 정비를 간편하게 예약하며, 이후에도 차량을 지속적으로 관리할 수 있는 통합된 서비스 경험이 필요하다.

GarageCare는 고객의 예약 편의성과 정비소의 운영 효율성을 함께 개선하는 것을 목표로 한다.

---

# 3. Business Strategy

## 3.1 Business Model

GarageCare의 초기 사업 모델은 특정 정비소에 적용되는 **단일 사업장 중심의 고객 관리 및 예약 서비스**이다.

초기에는 실제 정비소에서 사용 가능한 서비스를 구축하고 운영 효과를 검증한다.

이후 충분한 사용자 피드백과 운영 경험이 확보되면 다른 소규모 정비소로 확장할 수 있는 가능성을 검토한다.

### Initial Business Model

| 항목 | 내용 |
|---|---|
| Business Type | 정비소 운영 지원 및 고객 관리 서비스 |
| Initial Market | 실제 협력 정비소 1개 사업장 |
| Customer | 자동차 소유 고객 |
| Operator | 정비소 관리자 |
| Primary Channel | 반응형 웹사이트 |
| Revenue Model | 초기 단계에서는 별도 수익화보다 운영 검증에 집중 |
| Expansion | 복수 정비소 지원 및 서비스형 소프트웨어 전환 가능성 검토 |

## 3.2 Market Positioning

GarageCare는 자동차 정비소 검색이나 가격 비교보다 **고객과 특정 정비소의 지속적인 관계**에 집중한다.

고객은 GarageCare를 통해 신뢰하는 정비소의 정보를 확인하고, 자신의 차량을 등록하여 반복적으로 정비 서비스를 이용할 수 있다.

### Positioning Statement

> GarageCare는 자동차 소유 고객을 위한 정비 예약 및 차량 관리 서비스로, 간편한 예약과 일관된 차량 관리 경험을 통해 고객과 정비소의 지속적인 관계를 지원한다.

## 3.3 Competitive Differentiation

GarageCare가 추구하는 차별화 방향은 다음과 같다.

| 일반적인 예약 중심 서비스 | GarageCare가 지향하는 서비스 |
|---|---|
| 예약 신청 중심 | 예약과 차량 관리 경험 통합 |
| 일회성 서비스 이용 | 반복 이용을 고려한 차량 정보 관리 |
| 서비스 탐색 중심 | 고객과 정비소의 지속적인 관계 중심 |
| 예약 완료 중심 | 예약 이후 상태 확인 및 관리 중심 |

※ 위 비교는 제품 포지셔닝을 위한 개념적 구분이며, 실제 경쟁 서비스에 대한 조사 결과를 의미하지 않는다.

---

# 4. Target Users

## 4.1 Primary Persona — 일반 자동차 소유자

**사용자 특성**

- 자동차를 일상적인 이동 수단으로 이용한다.
- 정비에 대한 전문 지식이 많지 않을 수 있다.
- 정비소 방문 전에 필요한 정보를 확인하고 싶어 한다.
- 복잡한 입력이나 여러 단계의 예약 과정을 선호하지 않는다.

**Primary Needs**

- 정비소 위치와 영업시간 확인
- 정비 서비스 안내
- 간편한 정비 예약
- 예약 상태 확인
- 본인 차량 관리

**User Goal**

> 정비가 필요할 때 복잡한 과정 없이 예약하고, 내 차량의 정보를 편리하게 관리하고 싶다.

## 4.2 Secondary Persona — 다차량 보유 고객

**사용자 특성**

- 두 대 이상의 차량을 관리한다.
- 차량마다 정비 일정과 관리 정보가 다르다.
- 차량별 예약을 구분하여 확인하고 싶어 한다.

**Primary Needs**

- 복수 차량 등록
- 차량별 예약 조회
- 차량 정보 수정 및 관리
- 차량별 정비 이력 관리 기능(후속 단계)

## 4.3 Operator Persona — 정비소 관리자

**사용자 특성**

- 고객 문의와 예약을 관리한다.
- 차량 및 정비 서비스 정보를 확인한다.
- 정비 업무와 운영 업무를 병행한다.

**Primary Needs**

- 예약 목록 및 상태 관리
- 고객 및 차량 정보 조회
- 정비 서비스 관리
- 공지사항 전달
- 운영 현황 확인

---

# 5. Product Principles

GarageCare의 모든 고객용 화면은 다음 원칙을 기준으로 설계한다.

## 5.1 Care First

고객이 자신의 차량을 관리받고 있다고 느낄 수 있어야 한다.

이를 위해 마이페이지에서 등록 차량과 예약 현황을 쉽게 확인할 수 있도록 구성한다.

장기적으로는 정비 이력, 관리 주기 및 점검 알림 기능을 제공한다.

단, 실제로 구현되지 않은 관리 기능을 제공하는 것처럼 표현하지 않는다.

## 5.2 Simplicity

불필요한 기능과 입력을 최소화한다.

고객이 정비 예약을 진행할 때 필요한 정보만 요청하고, 기존에 등록한 차량 정보는 재사용한다.

## 5.3 Clarity

자동차 정비에 익숙하지 않은 고객도 이해할 수 있는 용어를 사용한다.

전문적인 용어가 필요한 경우 짧고 명확한 설명을 제공한다.

## 5.4 Consistency

메인 페이지, 차량 관리, 예약 및 마이페이지에서 일관된 디자인과 상호작용 방식을 제공한다.

버튼, 입력 양식, 상태 표시 및 내비게이션을 공통된 규칙으로 구성한다.

## 5.5 Trust

정비소 정보와 예약 상태를 명확하게 제공한다.

고객에게 잘못된 기대를 주지 않도록 실제 운영 방식과 제공 가능한 기능을 기준으로 정보를 표시한다.

## 5.6 Accessibility

모바일과 데스크톱에서 모두 이용할 수 있도록 설계한다.

시맨틱 HTML, 명확한 레이블, 키보드 접근성 및 충분한 텍스트 대비를 고려한다.

---

# 6. Information Architecture

## 6.1 Navigation Strategy

GarageCare의 고객용 내비게이션은 고객이 자주 이용하는 기능을 중심으로 구성한다.

### Global Navigation

```text
GarageCare

홈 | 정비 서비스 | 공지사항 | 예약하기 | 마이페이지
```

로그인 상태에 따라 우측 사용자 메뉴를 다르게 표시한다.

**비로그인 사용자**

- 로그인
- 회원가입

**로그인 사용자**

- 마이페이지
- 로그아웃

관리자 기능은 고객용 내비게이션과 구분하여 제공한다.

## 6.2 Sitemap

```text
GarageCare
│
├── Home
│   ├── Hero
│   ├── Service Overview
│   ├── Notice Preview
│   ├── Location
│   └── Contact Information
│
├── Services
│   ├── Service List
│   └── Service Detail
│
├── Notices
│   ├── Notice List
│   └── Notice Detail
│
├── Reservation
│   ├── Reservation Create
│   ├── Reservation List
│   └── Reservation Detail
│
├── My Page
│   ├── Overview
│   ├── My Vehicles
│   ├── My Reservations
│   └── Account Information
│
├── Authentication
│   ├── Login
│   └── Register
│
└── Admin
    └── Existing Admin Features
```

## 6.3 Navigation Priorities

고객이 가장 자주 수행할 것으로 예상되는 작업을 기준으로 우선순위를 설정한다.

| Priority | User Action | Entry Point |
|---|---|---|
| P0 | 정비 예약 | 메인 CTA, 상단 메뉴 |
| P0 | 예약 확인 | 마이페이지 |
| P0 | 차량 확인 및 등록 | 마이페이지 |
| P1 | 정비 서비스 확인 | 상단 메뉴, 메인 페이지 |
| P1 | 정비소 위치 및 연락처 확인 | 메인 페이지 |
| P1 | 공지사항 확인 | 메인 페이지, 상단 메뉴 |
| P2 | 계정 정보 관리 | 마이페이지 |

---

# 7. Functional Requirements

## 7.1 Home Page

### Objective

고객이 GarageCare의 목적과 정비소 정보를 빠르게 이해하고, 필요한 기능으로 이동할 수 있도록 한다.

### Requirements

| ID | Requirement | Priority |
|---|---|---|
| HOME-01 | GarageCare 브랜드 및 서비스 소개 | P0 |
| HOME-02 | 주요 예약 CTA 제공 | P0 |
| HOME-03 | 주요 정비 서비스 소개 | P0 |
| HOME-04 | 정비소 위치 및 연락처 안내 | P0 |
| HOME-05 | 영업시간 안내 | P0 |
| HOME-06 | 최신 공지사항 표시 | P1 |
| HOME-07 | 모바일 반응형 레이아웃 | P0 |

### UX Requirements

- 첫 화면에서 서비스의 목적을 이해할 수 있어야 한다.
- 예약하기 버튼을 쉽게 찾을 수 있어야 한다.
- 중요한 정비소 정보를 과도한 탐색 없이 확인할 수 있어야 한다.
- 모바일에서도 핵심 정보와 CTA가 명확하게 표시되어야 한다.

---

## 7.2 Maintenance Services

### Objective

고객이 정비소에서 제공하는 서비스를 이해하고 적절한 정비 항목을 선택할 수 있도록 한다.

### Requirements

| ID | Requirement | Priority |
|---|---|---|
| SERVICE-01 | 정비 서비스 목록 조회 | P0 |
| SERVICE-02 | 서비스명 및 간단한 설명 표시 | P0 |
| SERVICE-03 | 서비스별 상세 정보 제공 | P1 |
| SERVICE-04 | 예약 화면으로 이동 | P0 |

기존 MaintenanceItem 데이터를 재사용할 수 있는지 검토한다.

서비스 가격이나 소요 시간은 실제 운영 정책과 데이터가 확보된 경우에만 표시한다.

---

## 7.3 Notice

### Objective

정비소의 휴무일, 운영 변경 및 주요 안내사항을 고객에게 전달한다.

### Requirements

| ID | Requirement | Priority |
|---|---|---|
| NOTICE-01 | 공지사항 목록 조회 | P0 |
| NOTICE-02 | 공지사항 상세 조회 | P0 |
| NOTICE-03 | 메인 페이지 최신 공지 표시 | P1 |
| NOTICE-04 | 관리자 공지사항 등록 | P1 |
| NOTICE-05 | 관리자 공지사항 수정 및 삭제 | P1 |

공지사항의 저장 방식과 관리자 CRUD 구현 범위는 별도 구현 이슈에서 확정한다.

---

## 7.4 My Page

### Objective

고객이 자신의 차량과 예약 정보를 한곳에서 관리할 수 있도록 한다.

### Requirements

| ID | Requirement | Priority |
|---|---|---|
| MY-01 | 로그인 회원 정보 표시 | P0 |
| MY-02 | 등록 차량 목록 조회 | P0 |
| MY-03 | 차량 등록 화면 이동 | P0 |
| MY-04 | 예약 목록 조회 | P0 |
| MY-05 | 예약 상세 조회 | P0 |
| MY-06 | 예약 상태 표시 | P0 |
| MY-07 | 예약 취소 기능 연결 | P1 |
| MY-08 | 회원 정보 수정 | P2 |
| MY-09 | 차량별 정비 이력 | Future |
| MY-10 | 정비 주기 및 알림 | Future |

### Dashboard Concept

마이페이지는 단순한 기능 목록이 아니라 **내 차량 관리 현황을 확인하는 대시보드**로 구성한다.

예시:

```text
안녕하세요.

내 차량
────────────────────────
등록된 차량 2대

차량 A
차량 B

예약 현황
────────────────────────
다가오는 예약 1건

2026.11.05
차량 A · 엔진오일 교환
예약 상태: 확정

[예약 상세 보기]

빠른 메뉴
────────────────────────
[정비 예약] [차량 등록] [예약 내역]
```

※ 위 차량 수, 날짜, 정비 항목 및 예약 상태는 화면 구성을 설명하기 위한 예시 데이터이다.

### UX Requirements

- 고객이 등록 차량을 즉시 확인할 수 있어야 한다.
- 다가오는 예약이 있다면 쉽게 식별할 수 있어야 한다.
- 차량 등록과 예약 기능에 빠르게 접근할 수 있어야 한다.
- 예약 상태를 이해하기 쉬운 문구로 표시해야 한다.
- 다른 고객의 차량 및 예약 정보에 접근할 수 없어야 한다.

---

# 8. User Experience Design

## 8.1 Design Direction

GarageCare는 다음과 같은 디자인을 지향한다.

**Design Keywords**

- Clean
- Modern
- Reliable
- Calm
- Organized
- Caring

자동차 정비 서비스라는 특성상 전문성과 신뢰감을 전달하되, 지나치게 기계적이거나 복잡한 디자인은 지양한다.

고객이 편안하게 정보를 확인할 수 있도록 충분한 여백과 명확한 정보 계층을 활용한다.

## 8.2 Visual Identity

### Brand Personality

GarageCare는 다음과 같은 브랜드 이미지를 추구한다.

| Attribute | Direction |
|---|---|
| Professional | 전문적이고 신뢰할 수 있는 서비스 |
| Friendly | 자동차에 익숙하지 않은 고객도 편안한 서비스 |
| Organized | 정보가 명확하게 정리된 서비스 |
| Caring | 차량을 지속적으로 관리해 주는 서비스 |
| Modern | 깔끔하고 직관적인 디지털 경험 |

### Color Direction

초기 디자인 방향은 다음과 같이 설정한다.

- Primary: 차분한 블루 계열
- Background: 화이트 및 밝은 뉴트럴 계열
- Text: 짙은 그레이 계열
- Accent: 예약 및 주요 행동을 강조하는 색상
- Status: 예약 상태별 의미가 명확한 색상

정확한 색상 코드는 와이어프레임 및 디자인 시스템 단계에서 확정한다.

### Typography

- 가독성이 높은 산세리프 서체 사용
- 제목과 본문의 명확한 크기 구분
- 모바일 환경을 고려한 적절한 글자 크기
- 충분한 줄 간격과 여백 확보

## 8.3 Interaction Principles

- 주요 버튼의 역할을 명확하게 표시한다.
- 같은 행동에는 같은 디자인을 사용한다.
- 사용자가 수행한 작업의 결과를 즉시 안내한다.
- 오류 발생 시 원인과 해결 방법을 이해하기 쉽게 제공한다.
- 불필요한 팝업과 애니메이션을 최소화한다.
- 예약 과정에서 사용자의 입력 부담을 줄인다.

---

# 9. Core User Flows

## 9.1 First-Time Customer

```text
GarageCare 방문
        ↓
정비소 및 서비스 정보 확인
        ↓
예약하기 선택
        ↓
로그인 / 회원가입
        ↓
차량 등록
        ↓
정비 서비스 선택
        ↓
예약 신청
        ↓
예약 결과 확인
```

## 9.2 Returning Customer

```text
GarageCare 방문
        ↓
로그인
        ↓
마이페이지
        ↓
기존 차량 선택
        ↓
정비 예약
        ↓
예약 현황 확인
```

## 9.3 Reservation Management

```text
마이페이지
        ↓
내 예약 목록
        ↓
예약 상세 조회
        ↓
예약 상태 확인
        ↓
필요 시 예약 취소
```

## 9.4 User Flow Optimization

고객이 반복적으로 입력해야 하는 정보를 최소화한다.

- 등록 차량 정보 재사용
- 주요 화면에서 예약하기 접근 제공
- 마이페이지에 예약 현황 요약 제공
- 불필요한 페이지 이동 최소화
- 예약 결과와 상태를 명확하게 표시

---

# 10. Technical Requirements

## 10.1 Technology Stack

| Layer | Technology |
|---|---|
| Frontend | HTML5, CSS3 |
| Template Engine | Thymeleaf |
| Backend | Spring Boot, Java 17 |
| Persistence | Spring Data JPA |
| Local Database | H2 |
| Production Database | PostgreSQL |
| Build | Gradle |
| Infrastructure | Docker, AWS EC2 |
| CI | GitHub Actions |

## 10.2 Frontend Architecture

고객용 화면은 기존 Spring Boot 프로젝트의 Thymeleaf 템플릿을 기반으로 구성한다.

### Proposed Structure

```text
src/main/resources/
│
├── templates/
│   ├── home/
│   │   └── index.html
│   │
│   ├── services/
│   ├── notices/
│   ├── mypage/
│   │
│   └── fragments/
│       ├── header.html
│       └── footer.html
│
└── static/
    ├── css/
    │   ├── common.css
    │   ├── home.css
    │   └── mypage.css
    │
    ├── js/
    └── images/
```

※ 실제 프로젝트의 패키지 및 리소스 구조를 확인한 후 조정한다.

## 10.3 Web Standards

다음 웹표준기술을 활용한다.

- HTML5 문서 구조
- 시맨틱 태그
- 입력 양식 및 레이블
- CSS3 선택자
- CSS 레이아웃
- 반응형 웹 디자인
- 웹 접근성을 고려한 마크업

웹표준기술 수업의 공식 과제 요구사항이 공개되면 적용 범위를 재검토한다.

## 10.4 Security Requirements

- 로그인 사용자만 개인 차량 및 예약 정보에 접근할 수 있어야 한다.
- 고객은 본인 소유 데이터만 조회하고 변경할 수 있어야 한다.
- 관리자 기능은 기존 ADMIN 권한 정책을 유지해야 한다.
- 서버에서 요청 데이터와 사용자 권한을 검증해야 한다.
- 개인정보가 화면이나 로그에 불필요하게 노출되지 않아야 한다.

---

# 11. MVP Scope

## 11.1 In Scope — Sprint 6

Sprint 6에서는 고객용 웹사이트의 기본 사용 경험을 완성한다.

### Customer Experience

- 메인 페이지
- 정비 서비스 안내
- 공지사항 조회
- 정비소 위치 및 연락처 안내
- 고객 마이페이지
- 기존 차량 관리 기능 통합
- 기존 예약 관리 기능 통합
- 반응형 디자인

### Documentation

- 제품 기획서
- 사이트맵
- 사용자 흐름
- 와이어프레임
- 구현 및 검증 문서

## 11.2 Out of Scope — Sprint 6

다음 기능은 초기 개발 범위에서 제외한다.

- AI 기반 차량 정비 상담
- 차량 고장 예측
- 자동 정비 주기 추천
- 외부 결제 시스템
- 복수 정비소 지원
- 모바일 네이티브 애플리케이션
- 고급 고객 분석 시스템

## 11.3 Future Roadmap

향후 실제 운영 데이터를 확보한 후 다음 기능을 검토한다.

- 차량별 정비 이력 관리
- 엔진오일 등 소모품 교체 주기 알림
- 정비 예약 알림
- 개인화된 차량 관리 정보
- AI 기반 정비 상담
- 복수 정비소 지원

---

# 12. Success Metrics

## 12.1 Product Metrics

초기 운영 단계에서는 다음 지표를 수집한다.

| Metric | Description |
|---|---|
| Reservation Completion Rate | 예약을 시작한 사용자 중 예약을 완료한 비율 |
| Reservation Time | 예약 시작부터 완료까지 소요 시간 |
| Registration Completion Rate | 회원가입을 시작한 사용자 중 완료한 비율 |
| Returning Users | 일정 기간 내 재방문한 사용자 수 |
| Customer Feedback | 사용성 및 편의성에 대한 고객 의견 |

## 12.2 Operational Metrics

| Metric | Description |
|---|---|
| Online Reservations | 웹사이트를 통해 접수된 예약 수 |
| Reservation Processing | 관리자에 의해 처리된 예약 수 |
| Reservation Errors | 예약 처리 과정에서 발생한 오류 |
| Service Availability | 서비스 정상 운영 여부 |
| Support Requests | 사용 중 발생한 고객 문의 |

## 12.3 Initial Validation Targets

Sprint 7의 파일럿 운영에서 다음 목표를 검토한다.

- 실제 고객 5~10명 참여
- 실제 예약 10건 이상 처리
- 주요 예약 흐름의 정상 동작 확인
- 관리자 예약 처리 흐름 검증
- 사용자 피드백 수집
- 주요 사용성 문제 식별 및 개선

해당 수치는 초기 운영 목표이며, 실제 사업 성과를 입증한 결과가 아니다.

---

# 13. Risks & Constraints

## 13.1 Development Constraints

- 단일 개발자 중심의 개발 환경
- 학업과 병행하는 개발 일정
- 기존 백엔드 구조와의 호환성 유지 필요
- 실제 정비소 운영 정책 확인 필요

## 13.2 Product Risks

| Risk | Impact | Mitigation |
|---|---|---|
| 기능 범위 확대 | 일정 지연 | MVP 우선 개발 |
| 복잡한 사용자 흐름 | 예약 이탈 | 입력 및 이동 단계 최소화 |
| 기존 기능과 UI 불일치 | 사용성 저하 | 공통 디자인 적용 |
| 실제 운영 정보 부정확 | 고객 혼란 | 정비소 관리자 확인 |
| 권한 검증 누락 | 개인정보 노출 | 접근 제어 및 회귀 테스트 |
| 운영 환경 오류 | 서비스 이용 불가 | Sprint 7 운영 검증 |

## 13.3 Academic Constraints

웹표준기술 수업의 최종 과제 형식은 아직 확정되지 않았다.

현재 확인된 조건은 다음과 같다.

- 자유 주제의 웹사이트 제작
- 수업에서 학습한 기술 활용
- 웹사이트 기획문 작성
- 기획을 바탕으로 웹페이지 구현

공식 평가 기준과 제출 형식이 공개되면 본 기획서 및 구현 범위를 조정한다.

---

# 14. Development Roadmap

## Sprint 6 — Customer Experience & Web Design

```text
Customer Website Planning
        ↓
Sitemap & User Flow
        ↓
Wireframe Design
        ↓
Home Page Implementation
        ↓
Maintenance Service Pages
        ↓
Notice Pages
        ↓
Customer My Page
        ↓
Vehicle & Reservation UI Integration
        ↓
Responsive Design
        ↓
Customer Flow Verification
```

## Sprint 7 — Pilot Launch & Real-World Operations

```text
Production Readiness
        ↓
Security Verification
        ↓
Database Backup & Recovery
        ↓
Reservation Concurrency Verification
        ↓
Production Deployment
        ↓
Real-World Pilot
        ↓
Customer Feedback
        ↓
Operational Improvements
        ↓
Release v1.0.0
```

---

# 15. Open Questions

다음 사항은 실제 정비소 운영 환경과 구현 범위를 확인한 후 결정한다.

- 정비소의 공식 상호 및 브랜드 표기
- 정비소 주소 및 연락처
- 실제 영업시간과 휴무일
- 온라인 예약 가능 시간 및 운영 정책
- 예약 신청과 확정의 구분 방식
- 정비 서비스별 가격 및 소요 시간 표시 여부
- 공지사항 관리자 기능의 구현 범위
- 회원 정보 수정 기능의 구현 우선순위
- 실제 정비 이력 데이터의 수집 및 관리 방식
- 웹표준기술 수업의 최종 평가 기준

---

# 16. Conclusion

GarageCare의 핵심 가치는 단순히 자동차 정비 예약을 온라인으로 전환하는 데 있지 않다.

고객이 자신의 차량을 등록하고, 필요한 정비를 예약하며, 이후에도 차량 정보를 지속적으로 관리할 수 있는 경험을 제공하는 것이 GarageCare가 추구하는 방향이다.

Sprint 6에서는 고객 중심의 웹사이트를 완성하고, Sprint 7에서는 실제 정비소 운영을 통해 서비스의 안정성과 사용성을 검증한다.

이를 통해 GarageCare를 단순한 개발 프로젝트가 아닌 실제 고객과 정비소의 문제를 해결하는 서비스로 발전시킨다.
# GarageCare — Customer Website Sitemap & Information Architecture

| 항목 | 내용 |
|---|---|
| Project | GarageCare |
| Document | Customer Website Sitemap & Information Architecture |
| Version | 0.1.0 |
| Status | Draft |
| Milestone | Sprint 6 — Customer Experience & Web Design |
| Related Document | `docs/planning/customer-website-plan.md` |
| Last Updated | 2026-10-27 |

---

## 1. Overview

### 1.1 Purpose

본 문서는 GarageCare 고객용 웹사이트의 정보 구조(Information Architecture, IA), 페이지 계층, 내비게이션 및 사용자 접근 정책을 정의한다.

고객이 정비소 정보를 쉽게 확인하고, 차량 등록부터 정비 예약 및 예약 관리까지 직관적으로 이용할 수 있도록 페이지를 구성한다.

기존 Spring Boot 기반 회원·차량·예약 기능을 최대한 재사용하며, 새로운 고객용 화면과의 연결 구조를 명확히 한다.

### 1.2 Design Objectives

- 신규 고객에게 정비소의 신뢰성과 예약 편의성 제공
- 기존 고객에게 차량 중심의 지속적인 관리 경험 제공
- 주요 기능까지의 불필요한 이동 단계 최소화
- 공통 내비게이션과 일관된 정보 구조 유지
- 모바일 및 데스크톱 환경을 고려한 페이지 구성
- 기존 관리자 기능과 고객용 화면의 역할 분리
- 향후 정비 이력 및 차량 관리 알림 기능의 확장 가능성 확보

### 1.3 Scope

**In Scope — Sprint 6**

- 고객용 메인 페이지
- 정비 서비스 안내
- 공지사항 목록 및 상세
- 정비소 위치·연락처·영업시간 안내
- 회원가입 및 로그인 화면 연결
- 차량 등록 및 관리 화면 연결
- 예약 신청·조회·취소 화면 연결
- 고객 마이페이지
- 공통 Header, Navigation, Footer
- 반응형 내비게이션 구조

**Out of Scope — Sprint 6**

- AI 정비 상담
- 차량 고장 예측
- 자동 정비 주기 추천
- 차량별 실제 정비 이력 관리
- 자동 알림 시스템
- 온라인 결제
- 복수 정비소 지원

---

## 2. Information Architecture Principles

### 2.1 Customer-Centered Navigation

페이지는 시스템의 내부 기능이 아닌 고객의 이용 목적을 기준으로 구성한다.

고객이 원하는 작업을 쉽게 찾을 수 있도록 다음 기능을 중심으로 내비게이션을 설계한다.

1. 정비소 및 정비 서비스 확인
2. 정비 예약
3. 내 차량 관리
4. 예약 현황 확인
5. 공지사항 및 운영 정보 확인

### 2.2 Progressive Disclosure

첫 방문자에게 모든 차량 관리 기능을 노출하지 않는다.

비로그인 사용자에게는 정비소 소개, 서비스 안내 및 예약 기능을 우선적으로 제공한다.

로그인 사용자에게는 기존 정보를 활용하여 차량과 예약 현황을 추가로 제공한다.

단, 사용자 상태에 따라 웹사이트 전체 구조를 변경하지 않고 공통 레이아웃을 유지한다.

### 2.3 Minimal Navigation Depth

핵심 기능은 가능한 한 적은 단계로 접근할 수 있도록 설계한다.

- 메인 페이지에서 예약 신청 진입
- 상단 내비게이션에서 정비 서비스 조회
- 마이페이지에서 차량 및 예약 관리
- 메인 페이지에서 정비소 위치와 연락처 확인

### 2.4 Consistency

고객용 페이지에서 동일한 내비게이션, 버튼 및 상태 표현을 사용한다.

공통 UI 요소는 Thymeleaf Fragment로 분리하여 관리한다.

### 2.5 Separation of Responsibilities

고객용 웹사이트와 관리자 기능의 역할을 구분한다.

고객용 페이지는 본인의 차량 및 예약 관리에 집중하고, 관리자 페이지는 전체 고객·차량·예약 및 정비소 운영 관리에 집중한다.

---

## 3. Global Sitemap

### 3.1 Site Structure

```text
GarageCare
│
├── Home
│   ├── Hero
│   ├── Service Overview
│   ├── Notice Preview
│   ├── Location
│   └── Contact & Business Hours
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
│   │   ├── Vehicle List
│   │   ├── Vehicle Register
│   │   └── Vehicle Detail / Edit
│   │
│   ├── My Reservations
│   │   ├── Reservation List
│   │   └── Reservation Detail
│   │
│   └── Account Information
│
├── Authentication
│   ├── Login
│   └── Register
│
└── Admin
    ├── Dashboard
    ├── Reservation Management
    ├── Member Management
    ├── Vehicle Management
    └── Maintenance Item Management
```

### 3.2 Structure Policy

- `Home`, `Services`, `Notices`는 공개 페이지로 구성한다.
- `Reservation`은 로그인 회원의 예약 기능으로 구성한다.
- `My Page`는 로그인 회원의 개인 관리 영역으로 구성한다.
- `Authentication`은 회원가입 및 로그인 기능을 담당한다.
- `Admin`은 기존 관리자 기능을 유지한다.
- 정비소 위치와 연락처는 초기 MVP에서 별도 페이지를 만들지 않고 메인 페이지에 통합한다.
- 마이페이지의 차량·예약 메뉴는 기존 기능을 재사용하며 동일한 데이터를 위한 중복 구현을 방지한다.

---

## 4. Global Navigation

### 4.1 Header Navigation

고객용 웹사이트의 기본 상단 메뉴는 다음과 같이 구성한다.

```text
[GarageCare]   정비 서비스   공지사항   예약하기   마이페이지   [로그인]
```

로그인 상태에 따라 사용자 메뉴를 변경한다.

**Guest**

```text
[GarageCare]   정비 서비스   공지사항   예약하기   [로그인]
```

**Authenticated User**

```text
[GarageCare]   정비 서비스   공지사항   예약하기   마이페이지   [사용자 메뉴]
```

관리자는 필요 시 사용자 메뉴에서 관리자 페이지로 이동할 수 있도록 한다.

### 4.2 Navigation Behavior

| 메뉴 | 목적 | 비로그인 | 로그인 |
|---|---|---|---|
| Logo | 메인 페이지 이동 | 허용 | 허용 |
| 정비 서비스 | 정비 항목 확인 | 허용 | 허용 |
| 공지사항 | 정비소 공지 확인 | 허용 | 허용 |
| 예약하기 | 예약 신청 | 로그인 유도 | 예약 화면 이동 |
| 마이페이지 | 개인 차량 및 예약 관리 | 로그인 유도 | 허용 |
| 로그인 | 인증 | 허용 | 사용자 메뉴로 대체 |
| 사용자 메뉴 | 마이페이지·로그아웃 등 | 미표시 | 허용 |

### 4.3 Footer Navigation

Footer에는 고객이 자주 확인하는 정비소 운영 정보를 제공한다.

- 정비소 상호
- 정비소 주소
- 대표 연락처
- 영업시간 및 휴무일
- 주요 페이지 바로가기
- 개인정보처리방침
- 필요한 법정 사업자 정보

실제 운영 정보는 정비소 관리자에게 확인한 후 반영한다.

### 4.4 Mobile Navigation

모바일에서는 메뉴 항목을 축약하여 제공한다.

- 브랜드 로고 유지
- 주요 예약 CTA의 접근성 확보
- 나머지 메뉴는 모바일 내비게이션으로 제공
- 터치 영역과 메뉴 간격 확보
- 불필요한 중첩 메뉴 최소화

---

## 5. Page Inventory

### 5.1 Public Pages

| Page ID | Page | Purpose | Priority |
|---|---|---|---|
| HOME-01 | Home | 정비소 소개 및 주요 기능 진입 | P0 |
| SERVICE-01 | Service List | 제공 정비 서비스 확인 | P0 |
| SERVICE-02 | Service Detail | 개별 정비 서비스 정보 확인 | P1 |
| NOTICE-01 | Notice List | 공지사항 확인 | P0 |
| NOTICE-02 | Notice Detail | 공지사항 상세 조회 | P0 |
| AUTH-01 | Login | 회원 인증 | Existing |
| AUTH-02 | Register | 회원가입 | Existing |

### 5.2 Customer Pages

| Page ID | Page | Purpose | Priority |
|---|---|---|---|
| RES-01 | Reservation Create | 정비 예약 신청 | Existing |
| RES-02 | Reservation List | 본인 예약 목록 조회 | Existing |
| RES-03 | Reservation Detail | 본인 예약 상세 조회 | Existing |
| MY-01 | My Page Overview | 차량 및 예약 현황 요약 | P0 |
| VEH-01 | My Vehicle List | 등록 차량 목록 조회 | Existing |
| VEH-02 | Vehicle Register | 차량 등록 | Existing |
| VEH-03 | Vehicle Detail / Edit | 차량 상세 및 수정 | Existing |
| MY-02 | Account Information | 회원 정보 확인 | P1 |

`Existing`은 기존 기능을 재사용하는 것을 우선으로 한다는 의미이며, 고객용 화면의 추가 개선이 필요할 수 있다.

### 5.3 Admin Pages

| Page | Purpose | Status |
|---|---|---|
| Admin Dashboard | 운영 현황 확인 | Existing |
| Reservation Management | 전체 예약 관리 | Existing |
| Member Management | 회원 관리 | Existing |
| Vehicle Management | 차량 관리 | Existing |
| Maintenance Item Management | 정비 항목 관리 | Existing |
| Notice Management | 공지사항 관리 | Planned |

---

## 6. Page Responsibilities

### 6.1 Home

**Purpose**

고객이 GarageCare와 정비소의 주요 정보를 확인하고 필요한 서비스로 이동할 수 있도록 한다.

**Content**

- 브랜드 소개
- 주요 예약 CTA
- 정비 서비스 요약
- 최신 공지사항
- 정비소 위치
- 연락처 및 영업시간

**Behavior**

- 비로그인 사용자에게 정비소 소개와 예약 편의성을 강조한다.
- 로그인 사용자에게는 차량 및 예약 현황으로 이동할 수 있는 관리 영역을 추가한다.
- 공통 정보와 내비게이션 구조는 유지한다.

**Primary CTA**

`정비 예약하기`

**Secondary CTA**

`정비 서비스 알아보기`

---

### 6.2 Services

**Purpose**

고객이 제공되는 정비 서비스를 쉽게 확인할 수 있도록 한다.

**Content**

- 정비 항목명
- 정비 서비스 설명
- 필요 시 상세 정보
- 예약 화면 이동

**Design Policy**

- 자동차 전문 용어의 과도한 사용을 지양한다.
- 서비스 설명은 간결하게 작성한다.
- 실제 제공하지 않는 정비 서비스를 표시하지 않는다.
- 기존 MaintenanceItem 데이터를 활용하는 방안을 우선 검토한다.

---

### 6.3 Notices

**Purpose**

정비소 운영 및 서비스 관련 공지사항을 전달한다.

**Content**

- 공지 제목
- 작성일
- 공지 내용
- 필요 시 중요 공지 표시

**Design Policy**

- 최신 공지사항을 우선 표시한다.
- 메인 페이지에는 최근 공지사항 일부를 노출한다.
- 상세 페이지에서 전체 내용을 확인할 수 있도록 한다.
- 공지사항 등록·수정·삭제는 관리자 기능으로 분리한다.

---

### 6.4 Reservation

**Purpose**

고객이 등록된 차량을 기반으로 정비를 예약하고 예약 상태를 확인할 수 있도록 한다.

**Content**

- 차량 선택
- 정비 서비스 선택
- 예약 정보 입력
- 예약 결과
- 예약 상태
- 예약 상세 정보

**Design Policy**

- 기존 예약 기능을 최대한 재사용한다.
- 등록 차량 정보의 반복 입력을 방지한다.
- 예약 신청과 확정 상태를 명확하게 구분한다.
- 예약 취소 가능 여부는 실제 예약 정책에 따른다.

---

### 6.5 My Page

**Purpose**

고객이 자신의 차량과 예약 현황을 한곳에서 확인하고 관리할 수 있도록 한다.

**Content**

- 고객 환영 메시지
- 등록 차량 요약
- 다가오는 예약
- 예약 상태
- 빠른 예약 버튼
- 차량 관리 및 예약 내역 바로가기

**Design Policy**

- 계정 정보보다 차량 관리 정보를 우선 배치한다.
- 고객에게 필요한 다음 행동을 명확하게 안내한다.
- 차량이나 예약이 없는 경우 적절한 Empty State를 제공한다.
- 향후 정비 이력 및 관리 알림 기능을 확장할 수 있도록 구성한다.

**Primary CTA**

`내 차량 관리`

**Secondary CTA**

`정비 예약하기`

---

## 7. User Access Policy

### 7.1 Access Matrix

| Page / Feature | Guest | MEMBER | ADMIN |
|---|---|---|---|
| Home | Allow | Allow | Allow |
| Services | Allow | Allow | Allow |
| Notices | Allow | Allow | Allow |
| Login / Register | Allow | Redirect / Guide | Redirect / Guide |
| Reservation Create | Login Required | Allow | Allow |
| My Reservation List | Login Required | Own Data | Own Data |
| My Reservation Detail | Login Required | Own Data | Own Data |
| My Vehicle List | Login Required | Own Data | Own Data |
| Vehicle Register | Login Required | Allow | Allow |
| My Page | Login Required | Allow | Allow |
| Admin Pages | Login Required | Forbidden | Allow |

※ 관리자 계정도 고객용 기능을 이용할 경우 본인 소유 데이터에만 접근하도록 설계한다.

### 7.2 Authorization Principles

- 공개 정보 조회에는 로그인을 요구하지 않는다.
- 개인 차량 및 예약 정보에는 인증을 요구한다.
- 로그인 회원은 본인 소유 데이터만 조회·변경할 수 있어야 한다.
- 관리자 기능은 기존 ADMIN 권한 정책을 유지한다.
- 메뉴 노출 여부와 서버 측 접근 권한 검증은 별도로 처리한다.
- 인증되지 않은 사용자는 로그인 후 원래 요청한 기능으로 복귀할 수 있도록 설계한다.

---

## 8. Core Navigation Flows

### 8.1 Guest — First Reservation

```text
Home
  ↓
Service Information
  ↓
Reservation CTA
  ↓
Login / Register
  ↓
Vehicle Registration
  ↓
Reservation Create
  ↓
Reservation Result
```

### 8.2 Returning Customer — Reservation

```text
Home
  ↓
Login
  ↓
My Page
  ↓
Select Registered Vehicle
  ↓
Reservation Create
  ↓
Reservation Result
```

### 8.3 Returning Customer — Vehicle Management

```text
Home
  ↓
My Page
  ↓
My Vehicles
  ↓
Vehicle Detail
  ↓
Vehicle Edit
```

### 8.4 Reservation Status Check

```text
My Page
  ↓
My Reservations
  ↓
Reservation Detail
  ↓
Reservation Status
```

### 8.5 Guest — Business Information

```text
Home
  ↓
Service Overview
  ↓
Location / Contact / Business Hours
  ↓
Reservation CTA
```

정비소 정보 조회 과정에서는 불필요한 로그인을 요구하지 않는다.

---

## 9. URL Structure

다음 경로는 고객용 웹사이트의 제안 URL이며, 실제 구현 전에 기존 Controller 매핑과 충돌 여부를 확인한다.

| Page | Proposed URL | Access |
|---|---|---|
| Home | `/` | Public |
| Services | `/services` | Public |
| Service Detail | `/services/{id}` | Public |
| Notice List | `/notices` | Public |
| Notice Detail | `/notices/{id}` | Public |
| Login | `/members/login` | Public |
| Register | `/members/new` | Public |
| Reservation Create | `/reservations/new` | Authenticated |
| Reservation List | `/reservations` | Authenticated |
| Reservation Detail | `/reservations/{id}` | Owner |
| My Page | `/mypage` | Authenticated |
| My Vehicles | `/mypage/vehicles` | Authenticated |
| My Reservations | `/mypage/reservations` | Authenticated |
| Account Information | `/mypage/account` | Authenticated |
| Admin | `/admin` | ADMIN |

### URL Design Principles

- URL은 짧고 의미를 이해하기 쉽게 구성한다.
- 고객용 경로와 관리자 경로를 구분한다.
- 기존 Controller 경로는 가능한 한 유지한다.
- 마이페이지에서 기존 기능으로 이동할 경우 불필요한 중복 엔드포인트를 생성하지 않는다.
- 동일한 데이터 조회 로직을 여러 Controller에 중복 구현하지 않는다.

---

## 10. Frontend Integration Strategy

### 10.1 Thymeleaf Structure

고객용 페이지는 기존 Spring Boot 프로젝트에 통합한다.

```text
templates/
│
├── home/
│   └── index.html
│
├── services/
│   ├── list.html
│   └── detail.html
│
├── notices/
│   ├── list.html
│   └── detail.html
│
├── mypage/
│   ├── index.html
│   └── account.html
│
└── fragments/
    ├── header.html
    ├── navigation.html
    └── footer.html
```

기존 회원·차량·예약 템플릿은 현재 구조를 확인한 후 재사용하거나 개선한다.

### 10.2 CSS Structure

```text
static/css/
│
├── common.css
├── layout.css
├── components.css
├── home.css
├── services.css
├── notices.css
└── mypage.css
```

초기에는 필요한 스타일만 생성하며, 페이지별 파일 분리가 과도한 경우 통합하여 관리할 수 있다.

### 10.3 Reusable Components

- Header
- Navigation
- Footer
- Primary Button
- Secondary Button
- Service Card
- Notice List Item
- Vehicle Card
- Reservation Status Badge
- Empty State
- Form Field

공통 컴포넌트는 일관된 사용자 경험과 유지보수 효율을 위해 재사용한다.

---

## 11. Responsive Navigation

### 11.1 Desktop

- 상단 가로 내비게이션
- 주요 메뉴 직접 노출
- 예약 CTA 강조
- 사용자 메뉴 우측 배치

### 11.2 Mobile

- 로고 및 간소화된 메뉴
- 모바일 내비게이션 제공
- 예약 CTA 접근성 유지
- 차량 및 예약 정보의 세로 배치
- 충분한 터치 영역 확보

### 11.3 Responsive Principles

- 동일한 콘텐츠와 기능을 유지한다.
- 화면 크기에 따라 정보 배치를 조정한다.
- 모바일에서도 예약 흐름이 불편하지 않아야 한다.
- 중요한 정보가 화면 밖으로 숨겨지지 않아야 한다.
- 수평 스크롤이 발생하지 않도록 설계한다.

---

## 12. Navigation Validation

Sprint 6 구현 이후 다음 항목을 검증한다.

### Public Navigation

- [ ] 메인 페이지 정상 접근
- [ ] 정비 서비스 목록 접근
- [ ] 공지사항 목록 및 상세 접근
- [ ] 정비소 위치 및 연락처 확인
- [ ] 예약 CTA 정상 동작

### Authentication

- [ ] 비로그인 사용자의 보호 페이지 접근 제어
- [ ] 로그인 이후 요청한 페이지 복귀
- [ ] 로그인 상태별 내비게이션 표시
- [ ] 로그아웃 이후 보호 페이지 접근 차단

### Customer Navigation

- [ ] 마이페이지 정상 접근
- [ ] 등록 차량 목록 접근
- [ ] 차량 등록 및 수정 화면 이동
- [ ] 예약 신청 화면 이동
- [ ] 예약 목록 및 상세 조회
- [ ] 본인 소유 데이터만 조회 가능

### Responsive Navigation

- [ ] 데스크톱 메뉴 정상 동작
- [ ] 모바일 메뉴 정상 동작
- [ ] 주요 CTA 접근성 확인
- [ ] 모바일 화면에서 레이아웃 깨짐 없음

---

## 13. Implementation Priorities

| Phase | Scope | Priority |
|---|---|---|
| 1 | 공통 Header, Navigation, Footer | P0 |
| 2 | 메인 페이지 | P0 |
| 3 | 정비 서비스 안내 | P0 |
| 4 | 공지사항 목록 및 상세 | P0 |
| 5 | 마이페이지 Overview | P0 |
| 6 | 기존 차량·예약 화면 통합 | P0 |
| 7 | 반응형 디자인 및 접근성 | P0 |
| 8 | 관리자 공지사항 관리 | P1 |
| 9 | 회원 정보 관리 개선 | P2 |
| Future | 정비 이력 및 차량 관리 알림 | Future |

---

## 14. Open Questions

다음 항목은 구현 전 확인이 필요하다.

- 기존 회원·차량·예약 Controller의 실제 URL 매핑
- 기존 Thymeleaf 템플릿의 재사용 가능 범위
- 정비 서비스 상세 페이지의 필요 여부
- 공지사항 저장 및 관리자 관리 방식
- 예약 신청 이후 상태 표시 정책
- 마이페이지의 회원 정보 수정 범위
- 정비소의 실제 운영 정보
- 웹표준기술 수업 과제의 최종 요구사항

---

## 15. Conclusion

GarageCare의 고객용 웹사이트는 신규 고객과 기존 고객의 서로 다른 이용 목적을 하나의 일관된 정보 구조 안에서 지원한다.

신규 고객에게는 정비소의 신뢰성과 예약 편의성을 제공하고, 로그인 고객에게는 차량 및 예약 정보를 중심으로 지속적인 관리 경험을 제공한다.

이를 위해 공통 내비게이션과 명확한 페이지 계층을 유지하면서 사용자 상태에 따라 필요한 정보를 우선적으로 제공한다.

본 사이트맵을 기준으로 사용자 흐름과 와이어프레임을 구체화하고, Sprint 6의 고객용 웹사이트 구현을 진행한다.
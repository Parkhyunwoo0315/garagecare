# Feature List

> Version: 1.0.0
>
> Status: Draft
>
> Last Updated: 2026-07-15

---

# 1. Overview

이 문서는 GarageCare MVP에서 구현할 기능과 향후 확장 기능을 정의한다.

기능은 사용자 관점(Customer / Administrator)에서 분류하며,

MVP 범위를 명확하게 정의하여 개발 우선순위를 관리한다.

---

# 2. MVP Scope

GarageCare MVP는 실제 정비소에서 예약 업무를 수행할 수 있는 최소 기능 구현을 목표로 한다.

다음 기능을 MVP 범위로 정의한다.

| Domain | MVP |
|---------|:---:|
| Member | ✅ |
| Vehicle | ✅ |
| Reservation | ✅ |
| Notice | ✅ |
| Admin | ✅ |

---

## 3. User Stories

### Customer

- 고객은 원하는 날짜를 선택하여 예약할 수 있어야 한다.
- 고객은 자신의 예약 내역을 확인할 수 있어야 한다.
- 고객은 예약을 취소할 수 있어야 한다.

### Administrator

- 관리자는 모든 예약을 조회할 수 있어야 한다.
- 관리자는 예약 상태를 변경할 수 있어야 한다.

---

# 4. Feature List

## 👤 Member

회원 인증과 계정 관리를 담당한다.

| Feature | Description | Priority |
|-----------|------------|:--------:|
| 회원가입 | 새로운 계정을 생성한다. | High |
| 로그인 | 계정을 인증한다. | High |
| 로그아웃 | 세션을 종료한다. | Medium |

---

## 🚗 Vehicle

차량 정보를 관리한다.

| Feature | Description | Priority |
|-----------|------------|:--------:|
| 차량 등록 | 차량 정보를 등록한다. | High |
| 차량 조회 | 등록된 차량을 조회한다. | High |
| 차량 수정 | 차량 정보를 수정한다. | Medium |

---

## 📅 Reservation

예약을 관리한다.

| Feature | Description | Priority |
|-----------|------------|:--------:|
| 예약 등록 | 원하는 날짜에 예약한다. | High |
| 예약 조회 | 예약 정보를 조회한다. | High |
| 예약 취소 | 예약을 취소한다. | High |
| 예약 상태 확인 | 예약 진행 상태를 확인한다. | Medium |

---

## 👨‍🔧 Admin

관리자 기능을 담당한다.

| Feature | Description | Priority |
|-----------|------------|:--------:|
| 예약 조회 | 전체 예약을 조회한다. | High |
| 예약 상태 변경 | 예약 상태를 변경한다. | High |
| 고객 조회 | 고객 정보를 조회한다. | Medium |
| 차량 조회 | 차량 정보를 조회한다. | Medium |

---

## 📢 Notice

공지사항을 관리한다.

| Feature | Description | Priority |
|-----------|------------|:--------:|
| 공지사항 조회 | 공지사항을 확인한다. | High |
| 공지사항 등록 | 공지사항을 등록한다. | Medium |
| 공지사항 수정 | 공지사항을 수정한다. | Medium |
| 공지사항 삭제 | 공지사항을 삭제한다. | Medium |

---

# 5. Priority

기능은 다음 기준으로 우선순위를 관리한다.

| Priority | Description |
|-----------|-------------|
| High | MVP 구현에 반드시 필요한 기능 |
| Medium | MVP 이후 구현 가능하지만 우선 구현을 권장하는 기능 |
| Low | 추후 확장 기능 |

---

# 6. Future Features

다음 기능은 MVP 이후 구현한다.

## Maintenance

- 정비 이력 조회
- 소모품 관리
- 엔진오일 교체주기 관리

---

## AI

- AI 정비 상담
- 증상 기반 정비 추천
- FAQ 자동 응답

---

## Analytics

- Dashboard
- 예약 통계
- 고객 분석

---

# 7. Development Order

GarageCare는 다음 순서로 개발한다.

1. Member
2. Vehicle
3. Reservation
4. Admin
5. Notice

각 도메인은 테스트 완료 후 다음 단계로 진행한다.

---

# 8. Out of Scope

다음 기능은 MVP에서 제외한다.

- 온라인 결제
- 실시간 채팅
- 문자 알림
- 이메일 알림
- 다중 정비소 지원
- 재고 관리

---



# 9. Related Documents

- `planning.md`
- `domain-model.md`
- `erd.md`
- `api-spec.md`

feature-list.md는 구현 범위를 정의하는 문서이며,

상세 설계는 각 문서를 참고한다.
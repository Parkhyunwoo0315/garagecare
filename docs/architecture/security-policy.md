# GarageCare Security Policy

> Version: 1.0.0
> Status: Draft
> Last Updated: 2026-07-28

← [Docs](../README.md) / [Architecture](README.md)

---

# Overview

Security Policy는 GarageCare에서 사용자 정보와 서비스 데이터를 안전하게 보호하기 위한 보안 원칙을 정의합니다.

GarageCare는 인증(Authentication)과 인가(Authorization)를 명확히 구분하며, 최소 권한 원칙(Principle of Least Privilege)을 기반으로 시스템을 설계합니다.

보안은 특정 기능이 아니라 시스템 전반에 적용되는 기본 설계 원칙입니다.

---

# Goals

Security Policy의 목표는 다음과 같습니다.

- 사용자 정보를 안전하게 보호한다.
- 인증과 인가를 명확히 분리한다.
- 최소 권한 원칙을 적용한다.
- 민감한 정보를 외부에 노출하지 않는다.
- 향후 Spring Security 확장을 고려한 구조를 유지한다.

---

# Policy

GarageCare는 다음과 같은 보안 구조를 따릅니다.

```text
Client

↓

Authentication

↓

Authorization

↓

Controller

↓

Service

↓

Repository
```

모든 보호된 기능은 인증과 권한 확인을 거친 후 실행됩니다.

---

# Rules

## Authentication

인증(Authentication)은 사용자의 신원을 확인하는 과정입니다.

인증이 완료되어야 보호된 기능에 접근할 수 있습니다.

대표적인 인증 대상

- 로그인
- 로그아웃
- 세션 유지

향후 Spring Security 기반 인증을 적용할 예정입니다.

---

## Authorization

인가(Authorization)는 인증된 사용자가 해당 기능을 수행할 수 있는지 확인하는 과정입니다.

예시

- 자신의 예약만 조회 가능
- 자신의 예약만 취소 가능
- 관리자는 공지사항 관리 가능
- 일반 회원은 관리자 기능 접근 불가

---

## Password Policy

비밀번호는 평문으로 저장하지 않습니다.

모든 비밀번호는 암호화(Hashing) 후 저장합니다.

GarageCare는 BCrypt를 기본 암호화 방식으로 사용할 예정입니다.

---

## Sensitive Information

다음 정보는 외부에 직접 노출하지 않습니다.

- 비밀번호
- 암호화 정보
- 내부 식별자
- 예외 Stack Trace
- 데이터베이스 정보

응답에는 필요한 정보만 포함합니다.

---

## Session Management

로그인한 사용자는 인증 정보를 유지합니다.

향후

- Session 기반 인증
  또는
- JWT 기반 인증

으로 확장할 수 있도록 설계합니다.

---

## Access Control

권한은 최소 권한 원칙을 따릅니다.

| Role | Permission |
|------|------------|
| Guest | 로그인, 회원가입 |
| Member | 예약 조회, 예약 생성, 예약 취소 |
| Admin | 회원 관리, 공지사항 관리, 예약 관리 |

필요 이상의 권한은 부여하지 않습니다.

---

## Input Security

모든 사용자 입력은 검증합니다.

대표적인 검증 대상

- SQL Injection
- XSS
- 잘못된 입력
- 비정상 요청

입력 검증은 Validation Policy를 따릅니다.

---

## HTTPS

운영 환경에서는 HTTPS 사용을 원칙으로 합니다.

모든 로그인 및 개인정보 전송은 암호화된 연결을 사용합니다.

---

# Examples

## Example 1

비로그인 사용자가 예약 생성 요청

```text
로그인 안됨

↓

401 Unauthorized

↓

요청 거부
```

---

## Example 2

다른 회원의 예약 조회

```text
예약 소유자 불일치

↓

403 Forbidden

↓

조회 거부
```

---

## Example 3

관리자 기능 접근

```text
ROLE_MEMBER

↓

관리자 페이지 접근

↓

403 Forbidden
```

---

# Security Flow

보호된 기능은 다음 순서로 처리됩니다.

```text
Client

↓

Authentication

↓

Authorization

↓

Controller

↓

Service

↓

Repository
```

인증 또는 권한 확인에 실패하면 요청은 즉시 종료됩니다.

---

# Security Principles

GarageCare는 다음 원칙을 따릅니다.

- 인증과 인가를 분리한다.
- 최소 권한 원칙을 적용한다.
- 비밀번호는 암호화하여 저장한다.
- 민감한 정보를 응답에 포함하지 않는다.
- 인증되지 않은 요청은 보호된 기능에 접근할 수 없다.
- 권한이 없는 사용자는 리소스에 접근할 수 없다.
- 입력값은 항상 검증한다.
- 운영 환경에서는 HTTPS를 사용한다.

---

# Future Extension

GarageCare는 향후 다음 기능을 추가할 수 있도록 설계합니다.

- Spring Security
- JWT Authentication
- OAuth2 Login
- Refresh Token
- Remember Me
- Role Based Access Control(RBAC)
- API Rate Limiting
- CSRF Protection

기존 구조를 변경하지 않고 확장할 수 있도록 설계하는 것을 원칙으로 합니다.

---

# Related Documents

- Overview
- Layered Architecture
- Request Flow
- Validation Policy
- Exception Policy
- Logging Policy
- Design Principles
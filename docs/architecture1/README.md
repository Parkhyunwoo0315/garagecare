# GarageCare Architecture

> Version: 1.0.0
> Status: Draft
> Last Updated: 2026-07-28

---

## Overview

GarageCare의 아키텍처 문서를 정리한 디렉터리입니다.

이 문서는 프로젝트 구현에 앞서 애플리케이션의 전체 구조와 설계 원칙을 정의하기 위해 작성되었습니다.

도메인 모델, ERD, API 명세를 기반으로 계층 구조, 패키지 구성, 요청 처리 흐름, 트랜잭션, 예외 처리, 보안 정책 등을 문서화하여 구현 과정에서 일관된 개발 기준을 제공합니다.

---

## Goals

- Layered Architecture 기반의 구조 설계
- Package by Domain 기반의 패키지 구성
- 계층별 책임(Controller, Service, Repository)의 명확한 분리
- 공통 정책의 일관성 확보
- 유지보수성과 확장성을 고려한 설계

---

## Architecture Principles

GarageCare는 다음 설계 원칙을 기반으로 구현됩니다.

- Layered Architecture
- Package by Domain
- Domain-Driven Design(DDD)의 일부 개념 적용
- RESTful API 설계
- Spring Boot Best Practice
- 책임 기반 계층 분리(SRP)
- DTO를 통한 계층 간 데이터 전달
- Aggregate Root 중심의 도메인 관리

---

# Documents

## 📖 Overview

프로젝트의 전체 아키텍처와 설계 목표를 설명합니다.

> **overview.md**

---

## 📦 Package Structure

패키지 구성과 각 패키지의 역할을 정의합니다.

> **package-structure.md**

---

## 🏛️ Layered Architecture

애플리케이션 계층 구조와 계층 간 의존 관계를 정의합니다.

> **layered-architecture.md**

---

## 🔄 Request Flow

사용자의 요청이 시스템 내부에서 처리되는 전체 흐름을 설명합니다.

> **request-flow.md**

---

## 💾 Transaction Policy

트랜잭션 적용 기준과 범위를 정의합니다.

> **transaction-policy.md**

---

## ✅ Validation Policy

입력 검증과 비즈니스 검증의 책임을 정의합니다.

> **validation-policy.md**

---

## ⚠️ Exception Policy

예외 처리 전략과 공통 오류 응답 정책을 정의합니다.

> **exception-policy.md**

---

## 🔐 Security Policy

인증 및 권한 관리 정책을 정의합니다.

> **security-policy.md**

---

## 📝 Logging Policy

로그 기록 기준과 민감정보 처리 정책을 정의합니다.

> **logging-policy.md**

---

## 📏 Design Principles

GarageCare 프로젝트의 핵심 설계 원칙과 개발 규칙을 정의합니다.

> **design-principles.md**

---

# Reading Order

아키텍처 문서는 다음 순서로 읽는 것을 권장합니다.

1. Overview
2. Layered Architecture
3. Package Structure
4. Request Flow
5. Transaction Policy
6. Validation Policy
7. Exception Policy
8. Security Policy
9. Logging Policy
10. Design Principles

---

# Related Documents

프로젝트의 전체 설계 문서는 아래 문서와 함께 구성됩니다.

```text
docs/
├── planning.md
├── feature-list.md
├── domain-model.md
├── erd.md
├── wireframe.md
├── api-spec.md
└── architecture/
    ├── README.md
    ├── overview.md
    ├── package-structure.md
    ├── layered-architecture.md
    ├── request-flow.md
    ├── transaction-policy.md
    ├── validation-policy.md
    ├── exception-policy.md
    ├── security-policy.md
    ├── logging-policy.md
    └── design-principles.md
```

---

# Future Updates

아래 내용은 프로젝트 진행에 따라 지속적으로 보완될 예정입니다.

- Spring Security 설정
- 인증 및 권한 관리 고도화
- AOP 적용
- Redis 활용
- Docker 기반 실행 환경
- AWS 배포 구조
- 모니터링 및 로깅 시스템
- 성능 최적화 전략

---

# Notes

본 문서는 GarageCare의 구현 기준을 정의하는 설계 문서입니다.

코드 구현 과정에서 구조나 정책이 변경될 경우 관련 문서를 함께 수정하여 설계와 구현의 일관성을 유지합니다.
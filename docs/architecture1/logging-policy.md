# GarageCare Logging Policy

> Version: 1.0.0
> Status: Draft
> Last Updated: 2026-07-28

← [Docs](../README.md) / [Architecture](README.md)

---

# Overview

Logging Policy는 GarageCare에서 시스템 동작과 예외 상황을 기록하기 위한 로깅 기준을 정의합니다.

로그는 시스템의 상태를 확인하고 문제를 분석하는 중요한 운영 정보입니다. GarageCare는 필요한 정보만 기록하여 가독성을 높이고, 개인정보와 같은 민감한 정보는 기록하지 않는 것을 원칙으로 합니다.

---

# Goals

Logging Policy의 목표는 다음과 같습니다.

- 시스템 동작을 추적할 수 있도록 한다.
- 장애 발생 시 원인을 빠르게 분석할 수 있도록 한다.
- 로그의 일관성을 유지한다.
- 불필요한 로그를 최소화한다.
- 개인정보를 보호한다.

---

# Policy

GarageCare는 SLF4J와 Logback을 기본 로깅 프레임워크로 사용합니다.

모든 로그는 적절한 로그 레벨을 사용하며, 개발 환경과 운영 환경에서 동일한 기준을 따릅니다.

```text
Application

↓

Logger

↓

Console / Log File

↓

Monitoring
```

---

# Rules

## Logger Usage

모든 클래스는 Logger를 사용하여 로그를 기록합니다.

```java
private static final Logger log =
        LoggerFactory.getLogger(ReservationService.class);
```

System.out.println()은 사용하지 않습니다.

---

## Log Levels

GarageCare는 다음 로그 레벨을 사용합니다.

| Level | Description | Usage |
|--------|-------------|-------|
| TRACE | 매우 상세한 실행 흐름 | 디버깅 전용 |
| DEBUG | 개발 과정의 상세 정보 | 개발 환경 |
| INFO | 정상적인 서비스 동작 | 기본 로그 |
| WARN | 비정상 상황이지만 서비스는 계속 가능 | 경고 |
| ERROR | 서비스 처리 실패 | 예외 및 오류 |

---

## INFO Logging

정상적인 비즈니스 흐름을 기록합니다.

예시

- 회원가입 완료
- 예약 생성 완료
- 예약 취소 완료
- 로그인 성공

---

## WARN Logging

예상 가능한 문제를 기록합니다.

예시

- 존재하지 않는 회원 조회
- 중복 예약 요청
- 권한 없는 접근
- 잘못된 요청 데이터

WARN은 시스템 장애가 아닌 비정상 상황을 의미합니다.

---

## ERROR Logging

서비스가 정상적으로 처리되지 못한 경우 기록합니다.

예시

- 데이터베이스 오류
- 예외 발생
- 서버 내부 오류
- 외부 API 실패

ERROR 로그에는 예외(Stack Trace)를 함께 기록합니다.

---

## DEBUG Logging

개발 중에만 사용하는 로그입니다.

예시

- Request DTO 확인
- SQL 실행 흐름
- Service 실행 순서
- Transaction 시작 및 종료

운영 환경에서는 DEBUG 로그를 비활성화합니다.

---

# Logging Scope

GarageCare는 다음 상황에서 로그를 기록합니다.

| Category | INFO | WARN | ERROR |
|----------|------|------|--------|
| 회원가입 | ✓ | | |
| 로그인 | ✓ | | |
| 예약 생성 | ✓ | | |
| 예약 취소 | ✓ | | |
| 입력 오류 | | ✓ | |
| 권한 오류 | | ✓ | |
| 서버 오류 | | | ✓ |
| DB 오류 | | | ✓ |
| 예외 발생 | | | ✓ |

---

# Log Format

로그는 다음 정보를 포함합니다.

- Timestamp
- Log Level
- Class Name
- Message

예시

```text
2026-07-28 15:42:18 INFO ReservationService
Reservation created successfully.
```

---

# Sensitive Information

다음 정보는 로그에 기록하지 않습니다.

- 비밀번호
- 주민등록번호
- 인증 정보
- Access Token
- Refresh Token
- 개인정보 전체

필요한 경우 일부 값을 마스킹하여 기록합니다.

예시

```text
010-1234-****
```

---

# Examples

## Example 1

예약 생성

```text
INFO

Reservation created

memberId=1

reservationId=15
```

---

## Example 2

예약 시간 중복

```text
WARN

Duplicate reservation detected

memberId=1

reservationDate=2026-08-15
```

---

## Example 3

데이터베이스 오류

```text
ERROR

Database connection failed

SQLException ...
```

---

# Logging Flow

로그는 다음 순서로 기록됩니다.

```text
Client

↓

Controller

↓

Service

↓

Logger

↓

Log Output

↓

Monitoring
```

예외가 발생하면 Exception Handler에서도 로그를 기록합니다.

---

# Design Principles

GarageCare는 다음 원칙을 따릅니다.

- System.out.println()을 사용하지 않는다.
- Logger를 사용한다.
- 적절한 로그 레벨을 선택한다.
- 개인정보를 기록하지 않는다.
- 중복 로그를 남기지 않는다.
- ERROR 로그에는 예외 정보를 포함한다.
- 운영 환경에서는 DEBUG 로그를 비활성화한다.
- 로그는 문제 분석에 도움이 되는 정보만 기록한다.

---

# Future Extension

향후 서비스가 확장되면 다음 기능을 적용할 수 있도록 설계합니다.

- Logback Rolling Policy
- JSON Logging
- ELK Stack
- Grafana
- Prometheus
- OpenTelemetry
- Distributed Tracing
- Centralized Logging

기존 로깅 정책을 유지하면서 운영 환경에 맞게 확장하는 것을 원칙으로 합니다.

---

# Related Documents

- Overview
- Request Flow
- Transaction Policy
- Validation Policy
- Exception Policy
- Security Policy
- Design Principles
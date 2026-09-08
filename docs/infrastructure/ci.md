# Continuous Integration
> Version: 1.0.0  
> Status: Draft  
> Last Updated: 2026-09-08

---

> GarageCare Infrastructure  
> CI: GitHub Actions

---

## 1. Overview

GarageCare는 GitHub Actions를 이용하여
코드 변경사항에 대한 Build와 Test를 자동으로 검증한다.

Pull Request 또는 main 브랜치 변경 시
CI Workflow가 자동으로 실행된다.

---

## 2. CI Flow

```text
Push / Pull Request
        ↓
GitHub Actions
        ↓
Checkout
        ↓
Java 17
        ↓
Gradle
        ↓
Build
        ↓
CI Test
        ↓
Success / Failure
```

---

## 3. Trigger

CI는 다음 상황에서 실행된다.

```text
Pull Request → main

Push → main
```

Workflow:

```text
.github/workflows/ci.yml
```

---

## 4. Build

애플리케이션 Build는 다음 명령을 사용한다.

```bash
./gradlew clean build -x test --no-daemon
```

Build와 Test 단계를 분리하여
실패 원인을 쉽게 확인할 수 있도록 구성하였다.

---

## 5. Test

CI Test는 다음 명령으로 실행한다.

```bash
./gradlew ciTest --no-daemon
```

일반 Regression Test를 자동으로 검증한다.

PostgreSQL 실행계획 및 성능 측정을 목적으로 하는 실험 테스트는
일반 CI Test와 목적이 다르기 때문에 현재 CI 실행 대상에서 분리하였다.

---

## 6. PostgreSQL Performance Tests

다음 테스트들은 실제 PostgreSQL 환경과
실행계획 측정을 목적으로 한다.

```text
PostgreSqlSchemaTest
ReservationPostgreSqlIndexPerformanceTest
ReservationPaginationPerformanceTest
```

따라서 현재 기본 CI에서는 실행하지 않는다.

이 테스트들은 Database 성능 실험 및 수동 검증 단계에서 별도로 실행한다.

향후 필요할 경우 PostgreSQL Service Container를 사용하는
별도의 Integration Test Workflow로 분리할 수 있다.

---

## 7. Environment

CI 환경:

```text
OS: Ubuntu
Java: 17
Distribution: Eclipse Temurin
Build: Gradle Wrapper
```

GitHub Actions에서 동일한 Java/Gradle 환경을 구성하여
개발자 로컬 환경과 독립적으로 Build 가능 여부를 검증한다.

---

## 8. Security

Workflow의 기본 권한은 다음과 같이 제한한다.

```yaml
permissions:
  contents: read
```

현재 CI는 Production Database나 배포 환경에 접근하지 않으므로
Production Secret을 사용하지 않는다.

---

## 9. Failure

다음 중 하나라도 실패하면 CI Workflow가 실패한다.

```text
Compilation Failure
Build Failure
Test Failure
```

이를 통해 문제가 있는 변경사항을 Pull Request 단계에서 확인할 수 있다.

---

## 10. Related

### Workflow

```text
.github/workflows/ci.yml
```

### Infrastructure

```text
Dockerfile
compose.yaml
docs/infrastructure/docker.md
```

### Next

```text
Production Configuration & Secret Management
```
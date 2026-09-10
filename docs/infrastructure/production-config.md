# Production Configuration

> Version: 1.0.1  
> Status: Draft  
> Last Updated: 2026-09-06

---

> GarageCare Infrastructure  
> Environment Strategy: Spring Profiles  
> Profiles: `dev`, `prod`

---

## 1. Overview

GarageCare는 개발 환경과 운영 환경의 설정을 분리하여 관리한다.

공통 설정은 `application.properties`에 두고,
환경별 설정은 Spring Profile을 이용하여 분리한다.

```text
application.properties
        │
        ├── application-dev.properties
        │       └── Local / Docker Development
        │
        └── application-prod.properties
                └── Production Environment
```

Profile은 애플리케이션 코드에 고정하지 않고
실행 환경의 `SPRING_PROFILES_ACTIVE` 환경 변수를 통해 선택한다.

---

## 2. Configuration Structure

```text
src/main/resources/
├── application.properties
├── application-dev.properties
└── application-prod.properties
```

### Common

```text
application.properties
```

환경과 관계없이 공통으로 적용되는 설정을 관리한다.

Example:

```properties
spring.application.name=garagecare
spring.jpa.open-in-view=false
```

---

## 3. Development Profile

개발 환경에서는 다음 Profile을 사용한다.

```text
dev
```

실행:

```bash
SPRING_PROFILES_ACTIVE=dev \
./gradlew bootRun
```

Database 접속 정보는 환경 변수를 우선 사용하고,
로컬 개발 편의를 위해 개발용 기본값을 사용할 수 있다.

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/garagecare}
spring.datasource.username=${DB_USERNAME:garagecare}
spring.datasource.password=${DB_PASSWORD:garagecare}
```

JPA Schema 전략:

```properties
spring.jpa.hibernate.ddl-auto=update
```

개발 환경에서는 Entity 변경사항을 Schema에 반영하면서
기존 데이터를 유지할 수 있도록 구성한다.

---

## 4. Production Profile

운영 환경에서는 다음 Profile을 사용한다.

```text
prod
```

실행 환경에서는 다음과 같이 Profile을 지정한다.

```bash
SPRING_PROFILES_ACTIVE=prod
```

Production Database 접속 정보는 모두 환경 변수로 전달한다.

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

운영 환경에서는 기본값을 제공하지 않는다.

따라서 필수 환경 변수가 설정되지 않으면
애플리케이션이 정상적으로 시작되지 않는다.

---

## 5. Fail Fast

Production 환경에서는 Database 설정이 누락된 상태로
애플리케이션이 실행되는 것을 방지한다.

예를 들어:

```bash
SPRING_PROFILES_ACTIVE=prod \
./gradlew bootRun
```

을 실행했지만 다음 환경 변수가 없다면:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

애플리케이션 시작 과정에서 설정 오류가 발생한다.

```text
Production Start
      ↓
Required Secret Check
      ↓
Missing
      ↓
Application Startup Failure
```

이는 잘못된 Database 또는 개발 환경으로
운영 애플리케이션이 실행되는 것을 방지하기 위한 의도된 동작이다.

---

## 6. JPA Schema Strategy

환경별 Hibernate Schema 전략은 다음과 같다.

```text
Development
    ↓
ddl-auto=update

Production
    ↓
ddl-auto=validate
```

Production:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

`validate`는 Entity와 Database Schema의 일치 여부를 확인하지만,
Hibernate가 운영 Database Schema를 직접 생성하거나 수정하지 않는다.

```text
Entity
   ↓
Schema Validation
   ↓
Match
 ├── Yes → Application Start
 └── No  → Application Failure
```

운영 환경에서 자동 Schema 변경으로 발생할 수 있는
데이터 손상 위험을 줄이기 위한 설정이다.

---

## 7. Database Migration

현재 Production 환경에서는 Hibernate Schema 자동 변경을 사용하지 않는다.

향후 실제 운영 단계에서는 다음 구조를 고려한다.

```text
Database Migration
        ↓
Flyway / Liquibase
        ↓
Production Schema
        ↓
Hibernate Validate
        ↓
Application Start
```

현재 Sprint에서는 Migration Tool을 도입하지 않고,
운영 Schema가 애플리케이션에 의해 자동 변경되지 않는 구조까지만 구성한다.

---

## 8. Environment Variables

GarageCare 운영에 필요한 주요 환경 변수:

```text
SPRING_PROFILES_ACTIVE
DB_URL
DB_USERNAME
DB_PASSWORD
```

Example:

```bash
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:postgresql://database-host:5432/garagecare
DB_USERNAME=garagecare
DB_PASSWORD=<secret>
```

실제 Password는 Source Code나 Git Repository에 저장하지 않는다.

---

## 9. Secret Management

다음 정보는 Repository에 Commit하지 않는다.

```text
Database Password
AWS Credentials
API Key
Private Key
Production Secret
```

로컬 개발용 Secret은 `.env`를 이용할 수 있다.

```text
.env
```

`.env`는 `.gitignore`에 의해 Git 추적 대상에서 제외한다.

공유 가능한 환경 변수 구조는:

```text
.env.example
```

에 기록한다.

`.env.example`에는 실제 Secret을 포함하지 않는다.

---

## 10. Docker

Docker Compose에서는 Spring Profile을 환경 변수로 전달한다.

```yaml
environment:
  SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-dev}
```

기본 Docker 개발 환경은:

```text
dev
```

를 사용한다.

Docker 내부 PostgreSQL 연결:

```text
jdbc:postgresql://db:5432/garagecare
```

여기서 `db`는 Docker Compose의 PostgreSQL Service Name이다.

---

## 11. Verification

### Development Profile

```bash
SPRING_PROFILES_ACTIVE=dev \
DB_PASSWORD=garagecare \
./gradlew bootRun
```

다음을 확인한다.

```text
Profile: dev
Database Connection: Success
Application Startup: Success
```

### Production Profile without Secrets

```bash
SPRING_PROFILES_ACTIVE=prod \
./gradlew bootRun
```

필수 Database 설정이 없으므로
애플리케이션 시작이 실패해야 한다.

### Production Profile with Environment Variables

```bash
SPRING_PROFILES_ACTIVE=prod \
DB_URL=jdbc:postgresql://localhost:5432/garagecare \
DB_USERNAME=garagecare \
DB_PASSWORD=garagecare \
./gradlew bootRun
```

Database Schema가 Entity와 일치한다면
`validate` 검증 이후 애플리케이션이 실행된다.

Schema가 일치하지 않으면 실행이 중단된다.

---

## 12. Security Verification

`.env` Git 추적 여부 확인:

```bash
git check-ignore -v .env
```

Git에서 `.env` 추적 여부 확인:

```bash
git ls-files .env
```

정상적인 경우 아무것도 출력되지 않는다.

환경 변수 사용 위치 확인:

```bash
git grep -n "DB_PASSWORD"
```

Password 관련 설정 확인:

```bash
git grep -ni "password"
```

실제 Production Secret이 Repository에 포함되지 않았는지 확인한다.

---

## 13. Configuration Flow

```text
Runtime Environment
        │
        ├── SPRING_PROFILES_ACTIVE
        ├── DB_URL
        ├── DB_USERNAME
        └── DB_PASSWORD
                ↓
        Spring Boot Config
                ↓
        application.properties
                +
        application-{profile}.properties
                ↓
        DataSource / JPA
                ↓
        PostgreSQL
```

---

## 14. Related

### Configuration

```text
src/main/resources/application.properties
src/main/resources/application-dev.properties
src/main/resources/application-prod.properties
```

### Infrastructure

```text
Dockerfile
compose.yaml
.env.example
```

### Documentation

```text
docs/infrastructure/docker.md
docs/infrastructure/ci.md
```

### Next

```text
AWS Deployment
```
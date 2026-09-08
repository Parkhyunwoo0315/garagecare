# Docker Environment
> Version: 1.0.0  
> Status: Draft  
> Last Updated: 2026-09-08

---

> GarageCare Infrastructure  
> Environment: Docker Compose  
> Database: PostgreSQL 17

---

## 1. Overview

GarageCare는 개발 및 배포 환경의 일관성을 확보하기 위해
Docker 기반 실행 환경을 사용한다.

Spring Boot Application과 PostgreSQL을 각각 Container로 실행하고,
Docker Compose를 통해 하나의 실행 환경으로 관리한다.

---

## 2. Architecture

```text
Client
  │
  │ localhost:8080
  ▼
GarageCare Application
  │
  │ jdbc:postgresql://db:5432/garagecare
  ▼
PostgreSQL
  │
  ▼
Docker Volume
```

Docker Compose 내부에서는 Service Name을 이용하여 통신한다.

따라서 Application Container에서 PostgreSQL에 접근할 때
`localhost`가 아닌 `db`를 사용한다.

---

## 3. Services

### GarageCare

```text
Service: app
Container: garagecare-app
Port: 8080
```

Spring Boot Application을 Docker Image로 Build하여 실행한다.

### PostgreSQL

```text
Service: db
Container: garagecare-db
Version: PostgreSQL 17
Port: 5432 (Docker internal)
```

PostgreSQL은 Application과 동일한 Docker Compose Network에서 실행된다.

---

## 4. Environment Variables

GarageCare는 Database 접속 정보를 코드에 직접 저장하지 않는다.

필요한 환경 변수:

```text
DB_NAME
DB_USERNAME
DB_PASSWORD
```

로컬 환경에서는 `.env` 파일을 사용한다.

Example:

```bash
cp .env.example .env
```

이후 `.env`에 실제 개발환경 값을 설정한다.

`.env`는 Git에 포함하지 않는다.

---

## 5. Build

GarageCare Docker Image를 직접 Build하려면:

```bash
docker build -t garagecare:local .
```

Image 확인:

```bash
docker image ls
```

---

## 6. Run

전체 환경 실행:

```bash
docker compose up -d --build
```

Container 상태 확인:

```bash
docker compose ps
```

Application 로그 확인:

```bash
docker compose logs app
```

Database 로그 확인:

```bash
docker compose logs db
```

---

## 7. Database

PostgreSQL Container 접속:

```bash
docker compose exec db \
  psql -U garagecare -d garagecare
```

Table 확인:

```sql
\dt
```

Reservation Index 확인:

```sql
SELECT
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'reservations';
```

---

## 8. Health Check

PostgreSQL Container는 `pg_isready`를 이용하여
Database 준비 상태를 확인한다.

Application Service는 Database가 Healthy 상태가 된 이후 시작한다.

```text
PostgreSQL Start
      ↓
Health Check
      ↓
Healthy
      ↓
GarageCare Start
```

이를 통해 PostgreSQL이 준비되기 전에 Spring Boot가 연결을 시도하는 문제를 줄인다.

---

## 9. Data Persistence

PostgreSQL 데이터는 Docker Volume에 저장한다.

```text
postgres-data
```

따라서:

```bash
docker compose down
```

후 다시:

```bash
docker compose up -d
```

해도 데이터가 유지된다.

Volume까지 제거하려면:

```bash
docker compose down -v
```

를 사용한다.

이 명령은 PostgreSQL 데이터를 삭제하므로 주의한다.

---

## 10. JPA Schema Strategy

개발 환경에서는 현재 다음 정책을 사용한다.

```properties
spring.jpa.hibernate.ddl-auto=update
```

이를 통해 Container 재시작 시 기존 Schema와 데이터를 유지한다.

운영 환경에서는 Hibernate에 의한 자동 Schema 변경을 사용하지 않고,
향후 Database Migration 도구와 `validate` 전략을 검토한다.

---

## 11. Dockerfile

GarageCare Docker Image는 Multi-stage Build를 사용한다.

```text
Builder
│
├── Java 17 JDK
├── Gradle
└── Spring Boot bootJar
       ↓
Runtime
│
├── Java 17 JRE
└── app.jar
```

이를 통해 Build 환경과 Runtime 환경을 분리한다.

---

## 12. Verification

Docker 환경 구성 후 다음 항목을 검증한다.

```text
Docker Image Build
        ↓
PostgreSQL Container Start
        ↓
PostgreSQL Health Check
        ↓
GarageCare Container Start
        ↓
Spring Boot → PostgreSQL Connection
        ↓
Database Schema Creation
        ↓
Application Access
        ↓
Container Restart
        ↓
Database Persistence
```

---

## 13. Related

### Docker

```text
Dockerfile
compose.yaml
.dockerignore
.env.example
```

### Database

```text
docs/database/database-performance.md
docs/database/postgresql-migration.md
```

### Next

```text
GitHub Actions CI
```
# Docker Troubleshooting
> Version: 1.0.1  
> Status: Draft  
> Last Updated: 2026-09-06

---

> GarageCare Docker 실행 환경을 구성하면서 발생한 문제와  
> 원인 분석, 해결 과정, 검증 결과를 기록한다.
>
> 이 문서는 단순한 오류 해결 방법뿐만 아니라  
> Docker Container Network, Spring Boot DataSource,
> PostgreSQL 연결 구조를 이해하고 동일한 문제를 재현·진단할 수 있도록 작성한다.

---

## 1. Overview

GarageCare는 Spring Boot Application과 PostgreSQL을
Docker Container로 실행할 수 있도록 Docker Compose 기반 개발 환경을 구성하였다.

기본 구조는 다음과 같다.

```text
Host
 │
 │ localhost:8080
 ▼
┌──────────────────────────────┐
│ Docker Network               │
│ garagecare_default           │
│                              │
│  garagecare-app              │
│  Spring Boot                 │
│        │                     │
│        │ db:5432             │
│        ▼                     │
│  garagecare-db               │
│  PostgreSQL 17               │
│        │                     │
│        ▼                     │
│  postgres-data Volume        │
│                              │
└──────────────────────────────┘
```

Docker Compose 구성 과정에서 PostgreSQL Container는 정상적으로 실행되었지만,
Spring Boot Container가 PostgreSQL에 연결하지 못하고 반복적으로 재시작되는 문제가 발생하였다.

문제 해결 과정에서 다음 사항을 확인하였다.

- Docker Container 내부에서 `localhost`가 의미하는 대상
- Docker Compose Service Name을 이용한 Container 간 통신
- Spring Boot DataSource 환경변수 분리
- PostgreSQL Health Check와 Application 시작 순서
- Docker Image Rebuild가 필요한 경우
- Hibernate Dialect 오류와 실제 Database Connection 오류의 관계
- Container 상태와 Application 상태의 차이

---

# 2. Environment

문제가 발생한 환경은 다음과 같다.

```text
Application     Spring Boot 4.1.0
Java            17
ORM             Hibernate ORM 7.4.1.Final
Database        PostgreSQL 17.11
Build Tool      Gradle
Container       Docker
Orchestration   Docker Compose
```

Docker Compose에서는 다음 두 Service를 실행한다.

```text
app
 └─ GarageCare Spring Boot Application

db
 └─ PostgreSQL
```

PostgreSQL Service에는 Health Check를 설정하여
Database가 실제 연결 가능한 상태가 된 이후 Application Container가 시작되도록 구성하였다.

---

# 3. Problem

## 3.1 PostgreSQL Container 실행

먼저 PostgreSQL Container를 실행하였다.

```bash
docker compose up -d db
```

Container 상태를 확인했을 때 PostgreSQL은 정상적으로 실행되었다.

```text
garagecare-db   Up (healthy)
```

직접 PostgreSQL에 접속하는 것도 가능했다.

```bash
docker compose exec db psql -U garagecare -d garagecare
```

```text
psql (17.11)
Type "help" for help.

garagecare=#
```

따라서 다음 항목은 정상이라고 판단할 수 있었다.

```text
PostgreSQL Image       정상
PostgreSQL Container   정상
Database 생성          정상
Database User          정상
Health Check           정상
```

---

## 3.2 Spring Boot Container 실행 실패

전체 Docker Compose 환경을 실행하였다.

```bash
docker compose up -d --build
```

Docker Image Build 자체는 정상적으로 완료되었다.

```text
Image garagecare:local   Built
Container garagecare-db  Healthy
Container garagecare-app Started
```

그러나 다음 명령으로 실제 Container 상태를 확인했을 때
Application Container가 정상 실행 상태가 아니었다.

```bash
docker compose ps
```

결과:

```text
garagecare-app   Restarting (1)
garagecare-db    Up (healthy)
```

즉 Docker가 Application Container 생성에는 성공했지만,
Container 내부에서 실행되는 Spring Boot Application이 종료되고 있었다.

---

# 4. Diagnosis

## 4.1 Container Log 확인

Application Container의 로그를 확인하였다.

```bash
docker compose logs --tail=200 app
```

Hibernate 초기화 과정에서 다음 오류가 발생하였다.

```text
Unable to determine Dialect without JDBC metadata
```

처음에는 Hibernate의 PostgreSQL Dialect 설정 문제처럼 보일 수 있었다.

그러나 Stack Trace를 더 확인했을 때
실제 Database Connection 오류를 발견하였다.

```text
Connection to localhost:5432 refused.
Check that the hostname and port are correct and that the postmaster
is accepting TCP/IP connections.
```

보다 직접적인 원인은 다음과 같았다.

```text
Caused by: java.net.ConnectException: Connection refused
```

따라서 Hibernate Dialect 오류 자체를 수정하기보다
왜 Application Container가 `localhost:5432`로 접근하고 있는지 확인하였다.

---

# 5. Root Cause

## 5.1 Localhost의 의미

기존 로컬 개발 환경에서는 PostgreSQL이 Mac에서 직접 실행되었기 때문에
다음 JDBC URL을 사용할 수 있었다.

```text
jdbc:postgresql://localhost:5432/garagecare
```

구조는 다음과 같다.

```text
Local Development

Spring Boot
    │
    │ localhost:5432
    ▼
PostgreSQL
```

Spring Boot와 PostgreSQL이 동일한 Host 환경에서 실행되기 때문에
`localhost`를 이용한 접근이 가능하다.

하지만 Docker 환경에서는 Application과 PostgreSQL이
서로 다른 Container에서 실행된다.

```text
Docker

┌───────────────────┐
│ garagecare-app    │
│                   │
│ localhost         │
│      │            │
│      └── 자기 자신 │
└───────────────────┘

┌───────────────────┐
│ garagecare-db     │
│ PostgreSQL :5432  │
└───────────────────┘
```

따라서 `garagecare-app` Container 내부에서:

```text
localhost:5432
```

는 PostgreSQL Container가 아니라
**Spring Boot Container 자신을 의미한다.**

Application Container에는 PostgreSQL Server가 실행되고 있지 않기 때문에
Connection Refused가 발생하였다.

---

## 5.2 Docker Compose Service Discovery

Docker Compose는 동일한 Compose Network에 연결된 Service 사이에서
Service Name을 Hostname으로 사용할 수 있도록 내부 DNS를 제공한다.

GarageCare의 PostgreSQL Service는 다음과 같이 구성되어 있다.

```yaml
services:

  db:
    image: postgres:17
```

따라서 Application Container에서 PostgreSQL에 접근할 때는:

```text
localhost
```

가 아니라:

```text
db
```

를 Hostname으로 사용해야 한다.

올바른 JDBC URL은 다음과 같다.

```text
jdbc:postgresql://db:5432/garagecare
```

구조적으로 표현하면 다음과 같다.

```text
garagecare-app
      │
      │ jdbc:postgresql://db:5432/garagecare
      │
      │ Docker Internal DNS
      ▼
     db
      │
      ▼
garagecare-db
PostgreSQL :5432
```

---

# 6. Why Docker Compose Environment Variable Was Not Applied

Docker Compose에는 이미 다음과 같은 환경변수가 설정되어 있었다.

```yaml
environment:
  DB_URL: jdbc:postgresql://db:5432/garagecare
  DB_USERNAME: garagecare
  DB_PASSWORD: ${DB_PASSWORD}
  SPRING_PROFILES_ACTIVE: dev
```

Docker Compose 설정 자체가 정상적으로 적용되는지는 다음 명령으로 확인하였다.

```bash
docker compose config
```

Compose가 해석한 설정에는 다음 값이 정상적으로 존재하였다.

```text
DB_URL: jdbc:postgresql://db:5432/garagecare
DB_USERNAME: garagecare
SPRING_PROFILES_ACTIVE: dev
```

그러나 Spring Boot 로그에서는 여전히 다음 주소를 사용하고 있었다.

```text
localhost:5432
```

원인은 Spring Boot DataSource 설정이 환경변수를 참조하지 않고
로컬 PostgreSQL 주소를 직접 사용하고 있었기 때문이다.

즉 다음과 같은 구조였다.

```text
Docker Compose

DB_URL=db:5432
     │
     │ 환경변수 전달
     ▼
Spring Boot Container
     │
     │
     └─ application-dev.properties
            │
            └─ localhost:5432 직접 사용

Result
→ DB_URL이 DataSource URL에 사용되지 않음
```

환경변수를 Container에 전달하는 것만으로는
Application이 자동으로 임의의 `DB_URL` 변수를 DataSource URL로 사용하는 것은 아니다.

Spring Boot 설정에서 해당 환경변수를 명시적으로 참조해야 한다.

---

# 7. Solution

## 7.1 DataSource 설정 변경

기존 DataSource 설정을 환경변수 기반으로 변경하였다.

### Before

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/garagecare
spring.datasource.username=garagecare
spring.datasource.password=garagecare
```

### After

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/garagecare}
spring.datasource.username=${DB_USERNAME:garagecare}
spring.datasource.password=${DB_PASSWORD:garagecare}
spring.datasource.driver-class-name=org.postgresql.Driver
```

핵심은 다음 표현식이다.

```properties
${DB_URL:jdbc:postgresql://localhost:5432/garagecare}
```

Spring Property Placeholder의 기본값을 이용하여
환경에 따라 서로 다른 Database Host를 사용할 수 있도록 하였다.

---

## 7.2 Local Development

환경변수 `DB_URL`이 존재하지 않는 일반 로컬 개발 환경에서는
콜론 뒤의 기본값을 사용한다.

```text
DB_URL
  │
  └─ 존재하지 않음
         │
         ▼
jdbc:postgresql://localhost:5432/garagecare
```

따라서 기존 로컬 개발 환경을 유지할 수 있다.

```text
Spring Boot
    │
    │ localhost:5432
    ▼
Local PostgreSQL
```

---

## 7.3 Docker Environment

Docker Compose에서는 다음 환경변수를 전달한다.

```yaml
environment:
  DB_URL: jdbc:postgresql://db:5432/garagecare
  DB_USERNAME: garagecare
  DB_PASSWORD: ${DB_PASSWORD}
  SPRING_PROFILES_ACTIVE: dev
```

따라서 Docker 환경에서는 기본값 대신 `DB_URL`이 사용된다.

```text
DB_URL
  │
  └─ jdbc:postgresql://db:5432/garagecare
         │
         ▼
Spring Boot DataSource
```

결과적으로:

```text
Local
→ jdbc:postgresql://localhost:5432/garagecare

Docker
→ jdbc:postgresql://db:5432/garagecare
```

와 같이 실행 환경에 따라 Database 연결 주소가 분리된다.

---

# 8. Docker Image Rebuild

DataSource 설정을 변경한 직후 다음 명령을 먼저 실행하였다.

```bash
docker compose restart
```

그러나 Dockerfile에서는 Application Source를 복사한 후
Gradle을 이용하여 실행 JAR을 생성한다.

```dockerfile
COPY src src

RUN ./gradlew clean bootJar --no-daemon
```

그리고 생성된 JAR을 Runtime Image에 복사한다.

```dockerfile
COPY --from=builder /app/build/libs/*.jar app.jar
```

즉 `application-dev.properties` 역시
Build 시점에 JAR 내부에 포함된다.

따라서:

```bash
docker compose restart
```

는 기존 Image를 이용하여 Container를 다시 시작할 뿐,
수정된 Application 설정을 포함하는 새로운 Image를 생성하지 않는다.

설정 변경을 반영하기 위해 Image를 다시 Build하였다.

```bash
docker compose down
docker compose up -d --build
```

---

# 9. Verification

## 9.1 Docker Image Build

수정 이후 Multi-stage Docker Build가 정상적으로 완료되었다.

```text
[builder 9/9] RUN ./gradlew clean bootJar --no-daemon
[stage-1 3/3] COPY --from=builder /app/build/libs/*.jar app.jar

Image garagecare:local Built
```

이를 통해 변경된 Spring Boot 설정이 포함된 새로운 JAR과
Docker Image가 생성되었음을 확인하였다.

---

## 9.2 Container 상태

다음 명령으로 Container 상태를 확인하였다.

```bash
docker compose ps
```

결과:

```text
garagecare-app   Up
garagecare-db    Up (healthy)
```

기존의:

```text
Restarting (1)
```

상태가 사라지고 Application Container가 정상적으로 유지되었다.

또한 Application Port Mapping도 정상적으로 적용되었다.

```text
0.0.0.0:8080->8080/tcp
```

---

## 9.3 HikariCP Connection 확인

Spring Boot 로그를 다시 확인하였다.

```bash
docker compose logs --tail=100 app
```

HikariCP가 PostgreSQL Connection을 정상적으로 생성하였다.

```text
HikariPool-1 - Starting...
HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection
HikariPool-1 - Start completed.
```

이를 통해 Spring Boot DataSource와 PostgreSQL 간의
실제 JDBC Connection이 성공했음을 확인하였다.

---

## 9.4 Database Connection 정보 확인

Hibernate 로그에서도 실제 연결 정보를 확인하였다.

```text
Database JDBC URL [jdbc:postgresql://db:5432/garagecare]
Database driver: PostgreSQL JDBC Driver
Database dialect: PostgreSQLDialect
Database version: 17.11
Default catalog/schema: garagecare/public
Isolation level: READ_COMMITTED
```

특히 다음 JDBC URL이 사용된 것을 통해:

```text
jdbc:postgresql://db:5432/garagecare
```

Spring Boot가 더 이상 `localhost`가 아닌
Docker Compose Service Name인 `db`를 이용하여 PostgreSQL에 접근하고 있음을 확인하였다.

---

## 9.5 JPA 초기화 확인

Database Connection 이후 JPA EntityManagerFactory도 정상적으로 초기화되었다.

```text
Initialized JPA EntityManagerFactory for persistence unit 'default'
```

따라서 다음 초기화 흐름이 모두 정상적으로 완료되었다.

```text
Spring Boot
     │
     ▼
HikariCP
     │
     ▼
PostgreSQL Connection
     │
     ▼
Hibernate
     │
     ▼
JPA EntityManagerFactory
```

---

## 9.6 Application Startup 확인

최종적으로 Embedded Tomcat과 GarageCare Application의
정상 실행을 확인하였다.

```text
Tomcat started on port 8080 (http) with context path '/'
Started GaragecareApplication
```

따라서 Spring Boot Container가 정상적으로 실행되고
Host의 `8080` Port를 통해 접근 가능한 상태가 되었다.

---

# 10. Hibernate Dialect Error Analysis

문제 발생 당시 가장 상위에서 보였던 오류는 다음과 같았다.

```text
Unable to determine Dialect without JDBC metadata
```

오류 메시지만 보면 다음과 같은 설정을 추가하는 방법을 생각할 수 있다.

```properties
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

하지만 이번 문제에서는 이를 추가하지 않았다.

Hibernate는 JDBC Connection을 통해 Database Metadata를 확인하고
사용 중인 Database에 적합한 Dialect를 결정할 수 있다.

실제 오류 발생 과정은 다음과 같았다.

```text
Spring Boot 시작
      │
      ▼
HikariCP Connection 생성
      │
      ▼
localhost:5432 접근
      │
      X
Connection Refused
      │
      ▼
JDBC Metadata 조회 실패
      │
      ▼
Hibernate Database 판별 실패
      │
      ▼
Dialect 결정 실패
```

따라서:

```text
Unable to determine Dialect
```

는 근본 원인이 아니라
Database Connection 실패로 인해 발생한 **2차 증상**이었다.

실제 DataSource 연결 문제를 해결한 이후 Hibernate는 별도 설정 없이
다음과 같이 PostgreSQL Dialect를 정상적으로 판단하였다.

```text
Database dialect: PostgreSQLDialect
```

따라서 불필요한 Dialect 설정을 추가하지 않았다.

---

# 11. PostgreSQL Constraint Warning

정상 연결 이후 Hibernate Schema 초기화 과정에서
다음 Warning이 발생하였다.

```text
constraint "uk_maintenance_item_name" of relation "maintenance_items" does not exist, skipping
constraint "uk_member_email" of relation "members" does not exist, skipping
constraint "uk_reservation_maintenance_item" of relation "reservation_items" does not exist, skipping
constraint "uk_vehicle_number" of relation "vehicles" does not exist, skipping
```

하지만 이후 다음 로그가 정상적으로 출력되었다.

```text
Initialized JPA EntityManagerFactory for persistence unit 'default'
Tomcat started on port 8080
Started GaragecareApplication
```

따라서 해당 Warning은 Application 시작을 중단시키는 오류가 아니며,
현재 Docker 실행 환경 구성의 Blocking Issue로 판단하지 않았다.

향후 Production Database 구성 단계에서는
Hibernate DDL 자동 생성 정책과 Schema Migration 전략을 별도로 검토한다.

---

# 12. Additional Issue - Port 8080 Conflict

Docker 환경 구성 과정에서 Application Container 실행 시
다음 오류도 발생하였다.

```text
ports are not available:
listen tcp 0.0.0.0:8080:
bind: address already in use
```

이는 GarageCare Docker Container가 Host의 `8080` Port를 사용하려 했지만
이미 다른 Process가 해당 Port를 사용하고 있었기 때문이다.

Compose에서는 다음 Port Mapping을 사용한다.

```yaml
ports:
  - "8080:8080"
```

따라서 Host의 `8080` Port가 비어 있어야 한다.

현재 Port 사용 Process는 다음 명령으로 확인할 수 있다.

```bash
lsof -nP -iTCP:8080 -sTCP:LISTEN
```

기존 IntelliJ Spring Boot Application 등
8080 Port를 사용하는 Process를 종료한 후 다시 실행하였다.

```bash
docker compose up -d
```

이후 다음 Port Mapping이 정상적으로 생성되었다.

```text
0.0.0.0:8080->8080/tcp
```

---

# 13. Container Started vs Application Started

이번 문제를 통해 Docker의 다음 출력만으로는
Application 정상 실행을 판단할 수 없다는 점을 확인하였다.

```text
Container garagecare-app Started
```

`Started`는 Docker가 Container Process를 시작했다는 의미이며,
Container 내부 Spring Boot Application이 정상적으로 초기화되었다는 의미는 아니다.

실제로 초기 문제 상황에서는:

```text
Container garagecare-app Started
```

가 출력되었지만 이후 상태는:

```text
garagecare-app   Restarting (1)
```

이었다.

따라서 Container 기반 Application 실행 검증 시에는
최소한 다음 두 가지를 함께 확인해야 한다.

```bash
docker compose ps
docker compose logs --tail=100 app
```

최종적인 정상 상태는 다음과 같이 판단한다.

```text
Docker Container
        │
        ├─ STATUS = Up
        │
        ▼
Application Log
        │
        ├─ HikariPool Start completed
        ├─ EntityManagerFactory initialized
        ├─ Tomcat started
        └─ Started GaragecareApplication
```

---

# 14. PostgreSQL Verification Commands

PostgreSQL Container에 직접 접속하려면 다음 명령을 사용한다.

```bash
docker compose exec db psql -U garagecare -d garagecare
```

현재 Connection 정보 확인:

```sql
\conninfo
```

Database 목록 확인:

```sql
\l
```

Schema 확인:

```sql
\dn
```

Table 확인:

```sql
\dt
```

Index 확인:

```sql
\di
```

PostgreSQL 종료:

```sql
\q
```

Docker 환경에서 Database 문제를 분석할 때
Application Log뿐만 아니라 PostgreSQL에 직접 접속하여
Database 상태를 함께 확인할 수 있다.

---

# 15. Recommended Troubleshooting Flow

향후 Docker 환경에서 Application과 Database 간 연결 문제가 발생하면
다음 순서로 확인한다.

```text
1. Container 상태 확인
        │
        ▼
docker compose ps
        │
        ▼
2. Database Health 확인
        │
        ▼
db → healthy ?
        │
        ▼
3. Application Log 확인
        │
        ▼
docker compose logs app
        │
        ▼
4. 최초 Database Connection Error 확인
        │
        ▼
Host / Port / Username / Password
        │
        ▼
5. Docker Compose 환경변수 확인
        │
        ▼
docker compose config
        │
        ▼
6. Spring Boot DataSource 설정 확인
        │
        ▼
spring.datasource.*
        │
        ▼
7. Container Network Hostname 확인
        │
        ▼
localhost ? / Service Name ?
        │
        ▼
8. 설정 변경 시 Image Rebuild
        │
        ▼
docker compose up -d --build
        │
        ▼
9. JDBC Connection 확인
        │
        ▼
HikariPool Start completed
        │
        ▼
10. Application 정상 실행 확인
        │
        ▼
Started GaragecareApplication
```

Stack Trace의 마지막 메시지만 수정하기보다
**가장 먼저 발생한 연결 오류부터 추적하는 방식**을 우선한다.

---

# 16. Final Architecture

문제 해결 이후 GarageCare의 Local Docker 실행 구조는 다음과 같다.

```text
                    Host
                     │
                     │ localhost:8080
                     ▼
        ┌───────────────────────────┐
        │ Docker                    │
        │                           │
        │ garagecare-app            │
        │ Spring Boot 4.1.0         │
        │ Java 17                   │
        │                           │
        │        │                  │
        │        │ JDBC             │
        │        │                  │
        │        │ db:5432          │
        │        ▼                  │
        │ garagecare-db             │
        │ PostgreSQL 17.11          │
        │                           │
        │        │                  │
        │        ▼                  │
        │ postgres-data             │
        │ Docker Volume             │
        │                           │
        └───────────────────────────┘
```

Application Container와 Database Container는
Docker Compose가 생성한 동일 Network에 연결된다.

Application에서는 Docker Compose Service Name인 `db`를 이용하여
PostgreSQL에 접근한다.

Host에서는 Application의 공개 Port인 `8080`을 통해 GarageCare에 접근한다.

PostgreSQL Port `5432`는 Container 간 내부 통신에 사용되며,
Application Container에서는 Host의 `localhost`를 통해 접근하지 않는다.

---

# 17. Result

이번 문제 해결을 통해 다음 사항을 검증하였다.

```text
Docker Multi-stage Build                 Verified
Spring Boot Docker Image                 Verified
PostgreSQL 17 Container                  Verified
Docker Compose Network                   Verified
PostgreSQL Health Check                  Verified
Environment Variable Injection           Verified
Spring Boot → PostgreSQL JDBC Connection Verified
Docker Service Name Resolution           Verified
Hibernate PostgreSQL Dialect Detection   Verified
JPA EntityManagerFactory Initialization  Verified
Tomcat Startup                           Verified
Host Port Mapping                        Verified
```

최종적으로 GarageCare는
로컬에 직접 실행되는 PostgreSQL 주소에 의존하지 않고
Docker Compose 내부 Network를 통해 Application과 Database가 통신할 수 있게 되었다.

---

# 18. Key Takeaways

### Docker Network

Docker Container 내부의 `localhost`는
Host Machine이나 다른 Container가 아니라 현재 Container 자신을 의미한다.

Container 간 통신에서는 Docker Compose Service Name을
Hostname으로 사용할 수 있다.

```text
Wrong

jdbc:postgresql://localhost:5432/garagecare


Correct

jdbc:postgresql://db:5432/garagecare
```

### Environment Configuration

실행 환경에 따라 달라지는 Database URL과 Credential은
Application Source에 고정하지 않고 환경변수로 주입한다.

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/garagecare}
```

이를 통해 Local Development와 Docker Development 환경을
동일한 Application Code로 지원할 수 있다.

### Docker Image

Application 설정이 JAR 내부에 포함되는 구조에서는
Source 또는 설정 파일 변경 후 Container Restart만으로 변경사항이 반영되지 않을 수 있다.

필요한 경우 Image를 다시 Build한다.

```bash
docker compose up -d --build
```

### Error Analysis

다음 Hibernate 오류:

```text
Unable to determine Dialect without JDBC metadata
```

만 보고 Dialect 설정을 추가하지 않는다.

이번 사례에서는 실제 원인이:

```text
Connection to localhost:5432 refused
```

였으며 Database Connection을 복구하자
Hibernate가 PostgreSQL Dialect를 자동으로 판단하였다.

### Container Verification

다음 출력:

```text
Container Started
```

만으로 Application 정상 실행을 판단하지 않는다.

반드시 Container 상태와 Application Log를 함께 확인한다.

```bash
docker compose ps
docker compose logs app
```

---

# 19. Related

### Infrastructure

```text
Dockerfile
compose.yaml
.dockerignore
.env.example
```

### Application Configuration

```text
src/main/resources/application.properties
src/main/resources/application-dev.properties
```

### Related Documentation

```text
docs/infrastructure/docker.md
docs/database/postgresql-migration.md
docs/database/database-performance.md
```

### Related Topics

```text
Docker Compose
Docker Network
Container DNS
Spring Boot Profiles
Environment Variables
Spring Data JPA
Hibernate
HikariCP
PostgreSQL
```
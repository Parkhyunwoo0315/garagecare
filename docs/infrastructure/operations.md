# GarageCare Operations
> Version: 1.0.0  
> Status: Draft  
> Last Updated: 2026-09-15

---

> GarageCare Production Health Check & Logging  
> Environment: AWS EC2 + Docker + Amazon RDS PostgreSQL  
> Status: Verified

---

## 1. Overview

GarageCare 운영 환경에서 애플리케이션과 Database의 상태를 확인하고,
컨테이너 장애 및 재시작 이후 정상 복구 여부를 판단할 수 있도록
Health Check와 운영 Logging 정책을 구성하였다.

운영 환경은 다음 구조로 동작한다.

```text
Client
  │
  ▼
AWS EC2
  │
  ▼
Docker Container
  │
  ▼
Spring Boot
  │
  ├── Spring Boot Actuator
  │       └── /actuator/health
  │
  ▼
Amazon RDS PostgreSQL
```

---

## 2. Spring Boot Actuator

운영 상태 확인을 위해 Spring Boot Actuator를 추가하였다.

```groovy
implementation 'org.springframework.boot:spring-boot-starter-actuator'
```

운영 환경에서는 필요한 Health endpoint만 외부에 노출한다.

```properties
management.endpoints.web.exposure.include=health
management.endpoint.health.show-details=never
management.health.db.enabled=true
```

상세 내부 정보를 노출하지 않고 서비스의 정상 동작 여부만 확인할 수 있도록 구성하였다.

---

## 3. Health Check

Health endpoint는 다음 경로를 사용한다.

```text
/actuator/health
```

EC2에서 다음 명령으로 확인하였다.

```bash
curl -f http://localhost:8080/actuator/health
```

검증 결과:

```json
{
  "groups": [
    "liveness",
    "readiness"
  ],
  "status": "UP"
}
```

Spring Boot 애플리케이션과 Health endpoint가 정상적으로 동작함을 확인하였다.

---

## 4. Database Health

운영 환경에서는 Amazon RDS PostgreSQL을 Database로 사용한다.

Spring Boot 실행 로그에서 HikariCP가 정상적으로 Connection을 생성하는 것을 확인하였다.

```text
HikariPool-1 - Starting...
HikariPool-1 - Added connection
HikariPool-1 - Start completed.
```

또한 Hibernate가 PostgreSQL Database 정보를 정상적으로 인식하였다.

```text
Database driver: PostgreSQL JDBC Driver
Database dialect: PostgreSQLDialect
Database version: 17.6
Default catalog/schema: garagecare/public
```

따라서 다음 연결 경로가 정상적으로 동작함을 확인하였다.

```text
Docker Container
      │
      ▼
Spring Boot
      │
      ▼
HikariCP
      │
      ▼
Amazon RDS PostgreSQL
```

---

## 5. Docker Health Check

Docker 자체에서도 애플리케이션 상태를 판단할 수 있도록
Production Compose에 Health Check를 구성하였다.

```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
  interval: 30s
  timeout: 5s
  retries: 3
  start_period: 60s
```

Docker runtime image에는 Health Check 실행을 위한 `curl`을 추가하였다.

컨테이너 실행 직후:

```text
Up 4 seconds (health: starting)
```

Health Check 완료 후:

```text
Up 41 seconds (healthy)
```

로 전환되는 것을 확인하였다.

---

## 6. Restart Recovery Verification

운영 중 컨테이너가 재시작되는 상황을 가정하여 복구 동작을 검증하였다.

```bash
docker compose -f compose.prod.yaml restart app
```

재시작 직후:

```text
Up 7 seconds (health: starting)
```

이후:

```text
Up 47 seconds (healthy)
```

상태로 정상 전환되었다.

이를 통해 컨테이너 재시작 이후에도 Spring Boot 애플리케이션이 다시 기동되고
Docker Health Check가 정상 상태를 감지하는 것을 확인하였다.

---

## 7. Production Logging

운영 환경에서는 개발 환경에서 사용하던 상세 SQL Logging을 제한한다.

```properties
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.properties.hibernate.generate_statistics=false

logging.level.org.hibernate.SQL=INFO
logging.level.org.hibernate.orm.jdbc.bind=INFO
```

운영 로그에서는 다음과 같은 애플리케이션 시작 정보를 확인할 수 있다.

```text
The following 1 profile is active: "prod"
HikariPool-1 - Start completed.
Initialized JPA EntityManagerFactory
Tomcat started on port 8080
Started GaragecareApplication
```

이를 통해 운영에 필요한 시작 및 장애 정보는 유지하면서
개발 단계의 과도한 SQL 및 Parameter Logging은 제한하였다.

---

## 8. Secret Verification

운영 Database 인증 정보는 Git Repository가 아닌 EC2의 `.env` 파일에서 관리한다.

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

`.env` 파일의 권한은 다음과 같이 제한하였다.

```bash
chmod 600 .env
```

또한 운영 로그에서 Password 및 DB Password 환경변수 노출 여부를 확인하였다.

```bash
docker compose -f compose.prod.yaml logs app | grep -i password

docker compose -f compose.prod.yaml logs app | grep -i "DB_PASSWORD"
```

두 명령 모두 출력이 발생하지 않았다.

따라서 Database Password가 애플리케이션 로그에 직접 노출되지 않는 것을 확인하였다.

---

## 9. Troubleshooting

### 9.1 Docker Compose Buildx Version

EC2에 Docker Compose Plugin을 구성한 이후 이미지 빌드 과정에서 다음 오류가 발생하였다.

```text
compose build requires buildx 0.17.0 or later
```

기존 Buildx 버전이 Compose 요구 버전보다 낮은 것이 원인이었다.

최신 Buildx Plugin을 설치하여 Docker Compose Build가 정상적으로 수행되도록 수정하였다.

검증 결과:

```text
Image garagecare:prod Built
Container garagecare-app Started
```

---

### 9.2 Production Database Connection Failure

초기 Production 실행 과정에서 컨테이너가 반복적으로 재시작되는 문제가 발생하였다.

```text
Restarting (1)
```

애플리케이션 로그에서는 다음 오류가 확인되었다.

```text
The connection attempt failed.
Unable to obtain isolated JDBC connection
UnknownHostException: <RDS_ENDPOINT>
```

원인은 EC2 `.env`의 `DB_URL`에 실제 Amazon RDS Endpoint가 아닌
placeholder 값이 설정되어 있었기 때문이다.

```text
<RDS_ENDPOINT>
```

이를 실제 RDS Endpoint로 교체하였다.

```text
jdbc:postgresql://[RDS-ENDPOINT]:5432/garagecare
```

수정 후 HikariCP가 정상적으로 Connection을 생성하였으며,
Spring Boot 애플리케이션도 정상적으로 시작되었다.

---

### 9.3 Hibernate Dialect Error

Database 연결 실패 과정에서 다음 Hibernate 오류도 함께 발생하였다.

```text
Unable to determine Dialect without JDBC metadata
```

처음에는 Hibernate Dialect 설정 문제처럼 보일 수 있으나,
실제 원인은 잘못된 RDS Endpoint로 인해 Hibernate가 Database JDBC Metadata를
조회할 수 없었던 것이었다.

문제 발생 흐름은 다음과 같다.

```text
Invalid RDS Endpoint
        │
        ▼
UnknownHostException
        │
        ▼
PostgreSQL Connection Failure
        │
        ▼
JDBC Metadata 조회 실패
        │
        ▼
Hibernate Dialect 판별 실패
```

따라서 별도의 `hibernate.dialect` 설정을 추가하지 않고
Database 연결 정보를 수정하여 문제를 해결하였다.

정상 연결 이후 Hibernate는 자동으로 다음 Dialect를 판별하였다.

```text
Database dialect: PostgreSQLDialect
```

---

## 10. Verification Result

최종 운영 검증 결과는 다음과 같다.

| Item | Result |
|---|---|
| Production Profile | PASS |
| Spring Boot Startup | PASS |
| RDS PostgreSQL Connection | PASS |
| JPA Schema Validation | PASS |
| Actuator Health | PASS |
| Docker Health Check | PASS |
| Restart Recovery | PASS |
| Password Log Exposure | PASS |
| Docker Production Build | PASS |

최종 Docker 상태:

```text
garagecare-app
Up (healthy)
```

최종 Health Check:

```json
{
  "groups": [
    "liveness",
    "readiness"
  ],
  "status": "UP"
}
```

---

## 11. Current Operations Architecture

```text
AWS EC2
 │
 ├── Docker
 │    │
 │    └── garagecare-app
 │          │
 │          ├── Spring Boot
 │          ├── Spring Boot Actuator
 │          └── Docker Health Check
 │
 └──────────────► Amazon RDS PostgreSQL
                   │
                   └── Private Database
```

현재 GarageCare 운영 환경은 애플리케이션 실행뿐만 아니라
Database 연결 상태 확인, Docker Health Check 및 재시작 복구 여부까지
검증할 수 있는 구조를 갖추었다.

---

## 12. Limitations & Next Step

현재 Health Check는 Docker와 Spring Boot Actuator를 기반으로 수행한다.

다음 항목은 현재 범위에 포함하지 않는다.

```text
CloudWatch Dashboard / Alarm
Prometheus / Grafana
Distributed Tracing
ELK / OpenSearch
Load Balancer
HTTPS
Auto Scaling
```

향후 실제 운영 필요성이 증가할 경우 별도의 Observability 단계에서 확장한다.

현재 다음 단계는 GarageCare의 1차 운영 배포 상태를 최종 검증하는 것이다.

```text
Next:
🚀 Deployment | GarageCare 1차 운영 배포 검증
```

---

## 13. Related

### Production Configuration

```text
src/main/resources/application-prod.properties
```

### Docker

```text
Dockerfile
compose.prod.yaml
```

### Infrastructure Documentation

```text
docs/infrastructure/
```

### Environment

```text
AWS EC2
Docker
Spring Boot
Amazon RDS PostgreSQL
```
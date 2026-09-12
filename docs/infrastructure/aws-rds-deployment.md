# Production Database
> Version: 1.0.0  
> Status: Draft  
> Last Updated: 2026-09-12

---

> GarageCare Production Database Infrastructure  
> Status: Completed  
> Environment: AWS EC2 + Amazon RDS for PostgreSQL  
> Last Updated: 2026-09-12

---

## 1. Overview

GarageCare의 Production Database를
Application 서버와 독립된 Amazon RDS PostgreSQL 환경으로 분리하였다.

개발 환경에서는 Docker Compose를 통해
Application과 PostgreSQL을 함께 실행하지만,
Production에서는 Database를 별도의 AWS Resource로 관리한다.

```text
Development

Docker Compose
├── GarageCare
└── PostgreSQL


Production

Internet
   │
   ▼
AWS EC2
   │
   │ PostgreSQL / TCP 5432
   ▼
Amazon RDS
└── PostgreSQL
    └── garagecare
```

이를 통해 Application 재배포와 Database의 Life Cycle을 분리하고,
Production 환경에 적합한 Database 구조를 구성하였다.

---

## 2. Goals

이번 작업의 목표는 다음과 같다.

- Amazon RDS PostgreSQL 구성
- EC2와 RDS 간 Database Connection 구성
- RDS Public Access 차단
- Security Group 기반 접근 제한
- Production Database Credential 외부화
- GarageCare Schema 및 Index 적용
- Hibernate Schema 자동 변경 방지
- 실제 EC2 → RDS 연결 검증

---

## 3. Environment

```text
Cloud        AWS
Region       ap-northeast-2 (Seoul)
Application  Amazon EC2
OS           Amazon Linux 2023
Runtime      Docker
Database     Amazon RDS for PostgreSQL
PostgreSQL   17
Database     garagecare
Port         5432
Profile      prod
```

---

## 4. Architecture

Production 환경에서는 Application과 Database를
서로 독립적인 AWS Resource로 구성하였다.

```text
                    Internet
                       │
                       ▼
                ┌─────────────┐
                │   AWS EC2   │
                │             │
                │ GarageCare  │
                │ Spring Boot │
                └──────┬──────┘
                       │
                       │ JDBC / TLS
                       │ TCP 5432
                       ▼
                ┌─────────────┐
                │ Amazon RDS  │
                │ PostgreSQL  │
                │             │
                │ garagecare  │
                └─────────────┘
```

Application Container 또는 EC2가 재배포되더라도
Database는 별도의 Resource로 유지된다.

---

## 5. Network & Security

RDS는 외부 Internet에 직접 공개하지 않았다.

```text
RDS Public Access
→ Disabled

PostgreSQL
→ TCP 5432

Database Access
→ GarageCare EC2 Security Group
```

구조:

```text
Internet
   │
   ▼
EC2
   │
   │ TCP 5432
   ▼
RDS
```

다음과 같이 PostgreSQL Port를 전체 Internet에 공개하지 않는다.

```text
5432 → 0.0.0.0/0
```

Database Credential 또한 Source Code에 포함하지 않고
환경 변수로 주입하도록 구성하였다.

```properties
SPRING_PROFILES_ACTIVE=prod

DB_URL=jdbc:postgresql://<RDS_ENDPOINT>:5432/garagecare
DB_USERNAME=<DATABASE_USER>
DB_PASSWORD=<DATABASE_PASSWORD>
```

---

## 6. Production Schema Policy

Production에서는 Hibernate가 Database Schema를
자동으로 변경하지 않도록 구성하였다.

### Development

```properties
spring.jpa.hibernate.ddl-auto=update
```

### Production

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Production Database가 비어 있더라도
`update` 또는 `create`로 변경하지 않는다.

검증된 Schema를 명시적으로 적용한 뒤,
Hibernate는 Entity와 Database Schema의 일치 여부만 검증한다.

---

## 7. Schema Migration

기존 PostgreSQL 환경에서 검증한 GarageCare Schema를
Schema-only Dump로 추출하였다.

```bash
docker compose exec -T db \
  pg_dump \
  -U garagecare \
  -d garagecare \
  --schema-only \
  --no-owner \
  --no-privileges \
  > garagecare-schema.sql
```

이를 Production RDS에 적용하였다.

```text
Local PostgreSQL
       │
       │ pg_dump --schema-only
       ▼
garagecare-schema.sql
       │
       ▼
Amazon RDS PostgreSQL
```

이를 통해 Hibernate의 자동 Schema 생성에 의존하지 않고
검증된 Database 구조를 Production 환경에 적용하였다.

---

## 8. Connection Verification

Application 배포 전에
EC2에서 RDS PostgreSQL로 직접 연결을 검증하였다.

PostgreSQL Client를 별도로 설치하지 않고
Docker Image를 임시 Client로 사용하였다.

```bash
docker run --rm -it postgres:17 \
  psql \
  -h <RDS_ENDPOINT> \
  -p 5432 \
  -U garagecare_admin \
  -d garagecare
```

연결 결과:

```text
psql 17.x
server 17.x

SSL connection
protocol: TLSv1.3

garagecare=>
```

이를 통해 다음 연결 경로가 정상적으로 동작함을 확인하였다.

```text
EC2
 ↓
DNS
 ↓
Security Group
 ↓
RDS
 ↓
TLS
 ↓
PostgreSQL
 ↓
garagecare
```

---

## 9. Schema & Index Verification

초기 RDS Database는 정상적으로 Empty Schema 상태였다.

```sql
\dt
```

```text
Did not find any relations.
```

Schema 적용 이후 GarageCare의 Table 생성 여부를 확인하였다.

```sql
\dt
```

또한 기존 PostgreSQL 성능 실험에서 사용한
Reservation Composite Index도 확인하였다.

```sql
SELECT
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'reservations';
```

주요 Index:

```text
idx_reservation_member_date_time
```

```text
(member_id, reservation_date, reservation_time)
```

따라서 개발 환경에서 검증한 Database 구조와
성능 최적화가 Production PostgreSQL에도 반영됨을 확인하였다.

---

## 10. Troubleshooting

Infrastructure 구성 과정에서
SSH 인증과 RDS DNS Resolution 문제를 경험하였다.

### SSH Authentication

```text
Permission denied (publickey)
```

Verbose SSH Log와 Private Key Fingerprint를 확인하여
Network 문제가 아닌 Key Pair 불일치 문제로 범위를 좁혔다.

새 Key Pair를 연결한 EC2를 구성하고
SSH 접속을 우선 검증하여 해결하였다.

### RDS DNS Resolution

```text
could not translate host name
to address
```

PostgreSQL 인증 이전의 DNS 단계 문제로 판단하고
RDS Endpoint를 재확인하여 해결하였다.

이를 통해 Database 연결 문제를 다음 단계로 분리하여
분석할 수 있음을 확인하였다.

```text
DNS
 ↓
Network
 ↓
Security Group
 ↓
PostgreSQL
 ↓
Authentication
 ↓
Schema
```

---

## 11. Verification

최종적으로 다음 항목을 검증하였다.

- [x] Amazon RDS PostgreSQL 구성
- [x] EC2 → RDS 연결
- [x] RDS Public Access 비활성화
- [x] Security Group 기반 5432 접근 제한
- [x] PostgreSQL Authentication
- [x] TLS Connection
- [x] Production Credential 외부화
- [x] GarageCare Schema 적용
- [x] Reservation Composite Index 확인
- [x] Hibernate `ddl-auto=validate` 유지
- [x] Production Schema Validation
- [x] Application Read / Write 검증
- [x] Application 재시작 후 데이터 유지 검증

---

## 12. Result

이번 작업을 통해 GarageCare의 Database 환경은 다음과 같이 발전하였다.

```text
H2
 ↓
Local PostgreSQL
 ↓
Docker PostgreSQL
 ↓
Performance Validation
 ↓
Amazon RDS PostgreSQL
```

단순히 PostgreSQL을 연결하는 것에서 끝나지 않고,

```text
Network
Security Group
DNS
TLS
Credential
Schema
Index
Persistence
```

까지 Production 환경에서 검증하였다.

GarageCare는 이제 Application과 Database의 Life Cycle이 분리된
Production Infrastructure 구조를 갖추게 되었다.

---

## 13. Future Improvements

현재는 Schema-only Dump를 이용하여
초기 Production Schema를 구성한다.

향후 Schema 변경이 누적되면
Flyway 등의 Database Migration Tool을 도입하여
Schema Version을 관리할 예정이다.

```text
Application
     │
     ▼
Migration
     │
     ▼
Database Schema
     │
     ▼
Hibernate Validate
```

추가 Infrastructure 개선 대상으로는 다음을 고려한다.

- Flyway Database Migration
- AWS Secrets Manager
- Health Check
- Application Logging
- Monitoring
- HTTPS

---

## 14. Related

### Infrastructure

```text
docs/infrastructure/docker.md
docs/infrastructure/production-config.md
docs/infrastructure/aws-deployment.md
```

### Database

```text
docs/database/database-performance.md
docs/database/postgresql-migration.md
docs/database/performance/
```

### Configuration

```text
src/main/resources/application.properties
src/main/resources/application-dev.properties
src/main/resources/application-prod.properties
```
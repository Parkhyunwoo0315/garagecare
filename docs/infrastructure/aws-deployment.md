# AWS Deployment

> Version: 1.0.1  
> Status: Draft  
> Last Updated: 2026-09-12

---

> GarageCare Infrastructure  
> Platform: AWS EC2  
> Runtime: Docker / Docker Compose  
> Application: Spring Boot  
> Database: PostgreSQL  
> Status: Initial AWS Deployment Completed

---

## 1. Overview

GarageCare를 로컬 개발 환경에서 AWS EC2 환경으로 이전하여
외부 네트워크에서 접근 가능한 애플리케이션 실행 환경을 구축한다.

기존에 구성한 Docker 환경과 Production Profile을 활용하여
EC2 내부에서 Spring Boot Application과 PostgreSQL을 Docker Compose로 실행한다.

이번 배포의 핵심 목표는 다음과 같다.

```text
Local Development
        ↓
GitHub Repository
        ↓
AWS EC2
        ↓
Docker Compose
        ↓
GarageCare + PostgreSQL
        ↓
External Client
```

이를 통해 GarageCare가 개발자의 로컬 환경에 의존하지 않고
AWS 서버에서 독립적으로 실행될 수 있는지 검증한다.

---

## 2. Architecture

현재 AWS 배포 구조는 다음과 같다.

```text
                       Internet
                          │
                          │ HTTP :8080
                          ▼
                 AWS Security Group
                          │
                          ▼
                      AWS EC2
                          │
                   Docker Compose
                          │
              ┌───────────┴───────────┐
              │                       │
              ▼                       ▼
       garagecare-app           garagecare-db
        Spring Boot              PostgreSQL
          :8080                    :5432
              │                       ▲
              │                       │
              └──── Docker Network ───┘
```

외부 Client는 EC2의 Public Network를 통해 GarageCare Application에 접근한다.

PostgreSQL은 외부 Client가 직접 접근하지 않고
Docker 내부 Network를 통해 Application에서만 접근한다.

---

## 3. AWS Environment

### Region

AWS EC2를 이용하여 GarageCare 실행 환경을 구성한다.

```text
AWS EC2
Amazon Linux 2023
```

### Instance

GarageCare Application과 PostgreSQL을 하나의 EC2 Instance에서 실행한다.

현재 단계에서는 배포 구조를 단순하게 유지하기 위해
Application Server와 Database Server를 별도로 분리하지 않는다.

향후 운영 구조에서는 PostgreSQL을 AWS RDS 등으로 분리할 수 있다.

---

## 4. Security Group

GarageCare 외부 접근을 위해 EC2 Security Group을 구성한다.

### SSH

```text
Protocol: TCP
Port: 22
Source: Trusted Client IP
```

SSH Port는 가능한 한 특정 Client IP에서만 접근할 수 있도록 제한한다.

### Application

초기 배포 검증을 위해 Spring Boot Port를 외부에서 접근할 수 있도록 구성한다.

```text
Protocol: TCP
Port: 8080
```

### PostgreSQL

PostgreSQL Port는 외부에 공개하지 않는다.

```text
5432
```

Application과 PostgreSQL 간 통신은 Docker Network 내부에서 처리한다.

현재 `8080` Port 공개는 초기 배포 검증을 위한 구조이며,
향후 Reverse Proxy와 HTTPS를 적용한 이후 외부 직접 접근을 제거할 예정이다.

---

## 5. EC2 Connection

AWS에서 생성한 SSH Private Key를 이용하여 EC2에 접속한다.

Private Key 권한을 제한한다.

```bash
chmod 400 ~/Downloads/garagecare-prod-key.pem
```

EC2 접속:

```bash
ssh -i ~/Downloads/garagecare-prod-key.pem \
  ec2-user@<EC2_PUBLIC_IP>
```

정상적으로 접속하면 다음과 같은 Shell 환경을 확인할 수 있다.

```text
[ec2-user@ip-xxx-xxx-xxx-xxx ~]$
```

SSH 연결 종료:

```bash
exit
```

---

## 6. Server Environment

EC2 운영체제:

```text
Amazon Linux 2023
```

필요한 Package를 설치한다.

```bash
sudo yum update -y
sudo yum install -y git
sudo yum install -y docker
```

Docker Service를 실행한다.

```bash
sudo service docker start
```

서버 재부팅 이후에도 Docker가 자동으로 실행되도록 설정한다.

```bash
sudo systemctl enable docker
```

`ec2-user`가 `sudo` 없이 Docker를 사용할 수 있도록 Docker Group에 추가한다.

```bash
sudo usermod -a -G docker ec2-user
```

Group 변경사항을 적용하기 위해 SSH 연결을 종료한 후 다시 접속한다.

Docker 실행 상태 확인:

```bash
docker info
```

---

## 7. Docker Compose

Docker Engine 설치 이후 Docker Compose 사용 여부를 확인한다.

```bash
docker compose version
```

초기 EC2 환경에서는 다음 오류가 발생하였다.

```text
docker: 'compose' is not a docker command.
```

### Cause

Docker Engine은 설치되어 있었지만
Docker Compose CLI Plugin이 설치되어 있지 않았다.

### Resolution

Docker Compose Plugin을 추가한 후 다음 명령을 통해 설치 여부를 확인하였다.

```bash
docker compose version
```

정상적인 Docker Compose Version이 출력되는 것을 확인하였다.

---

## 8. Docker Buildx

GarageCare Docker Image Build 과정에서 다음 오류가 발생하였다.

```text
compose build requires buildx 0.17.0 or later
```

### Cause

Docker Compose는 정상적으로 설치되어 있었지만,
Docker Image Build에 필요한 Buildx가 없거나 요구 버전보다 낮았다.

따라서 PostgreSQL Container는 실행되었지만
GarageCare Application Image는 Build되지 않았다.

당시 상태:

```text
PostgreSQL
    ↓
Healthy

GarageCare
    ↓
Image Build
    ↓
Buildx Version Error
    ↓
Application Container Not Created
```

### Resolution

Docker Buildx CLI Plugin을 설치하였다.

```bash
mkdir -p ~/.docker/cli-plugins
```

Buildx 설치 후 실행 권한을 부여하고:

```bash
chmod +x ~/.docker/cli-plugins/docker-buildx
```

Version을 확인하였다.

```bash
docker buildx version
```

이후 GarageCare Image Build를 다시 수행하였다.

```bash
docker compose up -d --build app
```

Buildx 문제 해결 이후 Docker Image Build가 정상적으로 진행되었다.

---

## 9. Repository Deployment

EC2에서 GarageCare Repository를 Clone한다.

```bash
git clone <GARAGECARE_REPOSITORY_URL>
```

Repository 이동:

```bash
cd garagecare
```

배포 대상 Branch를 확인한다.

```bash
git branch --show-current
```

최신 `main` Branch를 사용한다.

```bash
git switch main
git pull origin main
```

배포 당시 주요 Infrastructure 작업은 다음 순서로 반영되어 있었다.

```text
Docker Environment
        ↓
GitHub Actions CI
        ↓
Production Configuration
        ↓
AWS Deployment
```

---

## 10. Production Secret

Production 환경의 Database Credential은 Repository에 저장하지 않는다.

EC2 Server에 `.env` 파일을 생성한다.

```bash
nano .env
```

환경 변수 구조:

```properties
DB_NAME=garagecare
DB_USERNAME=garagecare
DB_PASSWORD=<SECRET>

SPRING_PROFILES_ACTIVE=prod
```

실제 Password는 Repository나 문서에 기록하지 않는다.

`.env` 파일 권한을 제한한다.

```bash
chmod 600 .env
```

확인:

```bash
ls -l .env
```

Git 추적 여부도 확인한다.

```bash
git status
```

`.env`가 Git 변경사항에 포함되지 않아야 한다.

---

## 11. Database Bootstrap

Production Profile은 다음 Hibernate 설정을 사용한다.

```properties
spring.jpa.hibernate.ddl-auto=validate
```

따라서 완전히 비어 있는 PostgreSQL Database에서는
Hibernate가 Table을 자동으로 생성하지 않는다.

초기 Schema가 존재하지 않는 경우 다음 문제가 발생할 수 있다.

```text
Empty PostgreSQL
        ↓
prod Profile
        ↓
ddl-auto=validate
        ↓
Schema Missing
        ↓
Application Startup Failure
```

초기 배포에서는 기존 개발 환경의 Schema 생성 전략을 이용하여
Database Schema를 준비한 후 Production Profile로 전환하였다.

현재 방식은 초기 배포를 위한 임시 Bootstrap 전략이다.

향후 운영 환경에서는 다음 구조로 변경하는 것이 적절하다.

```text
Flyway
   ↓
Schema Migration
   ↓
PostgreSQL
   ↓
Hibernate Validate
   ↓
Spring Boot
```

---

## 12. PostgreSQL Container

PostgreSQL Container를 먼저 실행한다.

```bash
docker compose up -d db
```

상태 확인:

```bash
docker compose ps
```

정상 상태:

```text
garagecare-db
Up (healthy)
```

Database Log 확인:

```bash
docker compose logs db
```

PostgreSQL이 정상적으로 요청을 받을 준비가 되었는지 확인한다.

```text
database system is ready to accept connections
```

---

## 13. GarageCare Image Build

Application Image를 Build한다.

```bash
docker compose build --no-cache app
```

실제 AWS 환경에서 Multi-stage Docker Build가 정상적으로 완료되었다.

주요 Build 과정:

```text
eclipse-temurin:17-jdk
        ↓
Gradle bootJar
        ↓
Spring Boot JAR
        ↓
eclipse-temurin:17-jre
        ↓
GarageCare Runtime Image
```

Build 완료 후:

```text
Image garagecare:local Built
```

상태를 확인하였다.

---

## 14. Docker Compose Startup

전체 Container를 실행한다.

```bash
docker compose up -d
```

실행 결과:

```text
Network garagecare_default Created
Container garagecare-db Healthy
Container garagecare-app Started
```

Container 상태 확인:

```bash
docker compose ps
```

---

## 15. Production Profile Verification

Application Log를 확인한다.

```bash
docker compose logs -f app
```

실제 배포 환경에서 다음 Profile이 활성화된 것을 확인하였다.

```text
The following 1 profile is active: "prod"
```

따라서 AWS EC2의 GarageCare는 Production Configuration을 사용하여 실행된다.

---

## 16. PostgreSQL Connection Verification

Application Startup 과정에서 PostgreSQL 연결이 정상적으로 이루어졌다.

확인된 Database 정보:

```text
Database JDBC URL
jdbc:postgresql://db:5432/garagecare

Database Driver
PostgreSQL JDBC Driver

Database Dialect
PostgreSQLDialect

Database Version
17.11

Isolation Level
READ_COMMITTED
```

HikariCP Connection Pool 역시 정상적으로 초기화되었다.

```text
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
```

이를 통해 다음 연결이 정상적으로 동작함을 확인하였다.

```text
GarageCare Container
        ↓
Docker Network
        ↓
PostgreSQL Container
        ↓
garagecare Database
```

---

## 17. Application Startup Verification

Spring Boot Application이 정상적으로 시작되었다.

```text
Tomcat initialized with port 8080
```

최종적으로:

```text
Tomcat started on port 8080 (http) with context path '/'
Started GaragecareApplication
```

로그를 확인하였다.

실제 Startup Time:

```text
10.31 seconds
```

따라서 다음 Application Runtime이 정상적으로 구성되었다.

```text
Java 17
    ↓
Spring Boot 4.1.0
    ↓
Hibernate 7.4.1
    ↓
PostgreSQL 17.11
```

---

## 18. Local EC2 Verification

EC2 내부에서 Application 응답을 확인하였다.

```bash
curl http://localhost:8080
```

초기 결과:

```json
{
  "status": 404,
  "error": "Not Found",
  "path": "/"
}
```

### Analysis

이는 Application Startup 실패가 아니다.

Spring Boot가 Port `8080`에서 요청을 정상적으로 받고 있었지만
GarageCare에 `/` URL Mapping이 존재하지 않아 발생한 `404`였다.

즉 다음 구간까지는 정상적으로 동작하고 있었다.

```text
EC2
 ↓
Docker
 ↓
GarageCare
 ↓
Tomcat :8080
 ↓
Spring MVC
```

---

## 19. Login URL Troubleshooting

처음에는 로그인 화면을 다음 주소로 요청하였다.

```text
/login
```

하지만 Spring Boot에서 `404 Not Found`가 발생하였다.

### Cause

`MemberController`는 Class Level에서 다음 Mapping을 사용한다.

```java
@RequestMapping("/members")
```

Login Method:

```java
@GetMapping("/login")
```

따라서 실제 URL은 두 Mapping의 조합이다.

```text
/members
    +
/login

    ↓

/members/login
```

### Resolution

올바른 Login URL:

```text
/members/login
```

으로 접근하였다.

---

## 20. External Network Troubleshooting

초기 외부 Browser 접근에서는 다음 오류가 발생하였다.

```text
ERR_CONNECTION_TIMED_OUT
```

EC2 내부에서는 Application이 정상적으로 응답하고 있었기 때문에
Application 자체의 문제는 아니었다.

### Analysis

```text
External Browser
       │
       │ :8080
       ▼
AWS Security Group
       │
       ▼
EC2
       │
       ▼
Docker
       │
       ▼
Spring Boot
```

EC2 내부 `localhost:8080` 요청은 정상적으로 처리되었으므로
외부 Network 구간을 점검하였다.

### Resolution

EC2 Security Group에서 Application Port에 대한
Inbound Rule을 구성하였다.

```text
TCP
8080
```

이후 외부 Browser에서 EC2 Application에 정상적으로 접근할 수 있었다.

---

## 21. External Access Verification

외부 Client에서 다음 형태의 주소로 접근하였다.

```text
http://<EC2_PUBLIC_IP>:8080/members/login
```

GarageCare Login Page가 정상적으로 표시되었다.

이를 통해 다음 전체 Network Flow가 정상적으로 동작함을 확인하였다.

```text
Mac Browser
      ↓
Internet
      ↓
EC2 Public Network
      ↓
Security Group
      ↓
TCP :8080
      ↓
Docker Port Mapping
      ↓
GarageCare Container
      ↓
Spring MVC
      ↓
Thymeleaf
      ↓
Login Page
```

이 검증을 통해 GarageCare가 로컬 개발 환경을 벗어나
AWS EC2에서 외부 Client에게 실제 Web Page를 제공할 수 있음을 확인하였다.

---

## 22. Deployment Result

최종 검증 결과:

```text
AWS EC2                     PASS
Amazon Linux 2023           PASS
SSH Connection              PASS

Docker Engine               PASS
Docker Compose              PASS
Docker Buildx               PASS

GarageCare Image Build      PASS
GarageCare Container        PASS
PostgreSQL Container        PASS
PostgreSQL Health Check     PASS

Spring Profile: prod        PASS
PostgreSQL Connection       PASS
JPA Initialization          PASS
Hibernate Schema Validation PASS

Tomcat :8080                PASS
Spring Boot Startup         PASS
EC2 Internal Request        PASS

Security Group              PASS
External TCP :8080          PASS
External HTTP Request       PASS
Thymeleaf Login Page        PASS
```

GarageCare의 최초 AWS 배포가 정상적으로 완료되었다.

---

## 23. Troubleshooting Summary

이번 배포 과정에서 다음 문제를 확인하고 해결하였다.

### SSH Key Path

```text
Problem
SSH Private Key를 현재 Directory에서 찾을 수 없음

Cause
Private Key가 Downloads Directory에 존재

Resolution
~/Downloads/garagecare-prod-key.pem 경로 사용
```

### EC2 Public IP Placeholder

```text
Problem
EC2_PUBLIC_IP Hostname Resolution 실패

Cause
예제 Placeholder를 실제 IP로 변경하지 않음

Resolution
실제 EC2 Public IPv4 주소 사용
```

### Docker Compose

```text
Problem
docker: 'compose' is not a docker command

Cause
Docker Compose CLI Plugin 미설치

Resolution
Docker Compose Plugin 설치
```

### Docker Buildx

```text
Problem
compose build requires buildx 0.17.0 or later

Cause
Buildx 미설치 또는 Version 부족

Resolution
Docker Buildx CLI Plugin 설치
```

### External Timeout

```text
Problem
ERR_CONNECTION_TIMED_OUT

Cause
EC2 Application Port에 대한 외부 접근 경로 미구성

Resolution
Security Group TCP 8080 Inbound Rule 구성
```

### Login 404

```text
Problem
/login → 404

Cause
MemberController Class Level Mapping 누락

@RequestMapping("/members")
@GetMapping("/login")

Resolution
/members/login 사용
```

---

## 24. Current Limitations

현재 배포는 최초 AWS 실행 검증을 목적으로 한다.

따라서 다음 한계가 존재한다.

### HTTP

현재 Application은:

```text
HTTP :8080
```

을 통해 직접 노출되어 있다.

Browser에서는 암호화되지 않은 HTTP 연결로 표시된다.

### Public Application Port

Spring Boot `8080` Port가 외부 Client 접근에 사용된다.

향후 Reverse Proxy를 적용하면 Spring Boot Port를 외부에 직접 공개하지 않는다.

### Database

현재 PostgreSQL은 EC2 내부 Docker Container에서 실행된다.

```text
EC2
├── GarageCare
└── PostgreSQL
```

Application Server와 Database Server가 동일한 EC2 Instance에 존재한다.

### Database Migration

현재 별도의 Schema Migration Tool을 사용하지 않는다.

향후 Flyway 등을 도입하여 Production Schema 변경을 명시적으로 관리할 필요가 있다.

### Public IP

현재 EC2 Public IP를 직접 이용하여 접근한다.

고정 주소 및 Domain 기반 접근 구조가 아직 구성되지 않았다.

---

## 25. Future Architecture

현재:

```text
Internet
   ↓
EC2 Public IP :8080
   ↓
GarageCare
   ↓
Docker PostgreSQL
```

향후 목표:

```text
                    Internet
                       │
                     HTTPS
                       │
                       ▼
                     Domain
                       │
                       ▼
                Reverse Proxy
                       │
                       ▼
                  GarageCare
                       │
                       ▼
                Production DB
```

추가적으로 다음 Infrastructure 개선을 고려한다.

```text
HTTPS / TLS
Reverse Proxy
Domain
Production PostgreSQL / RDS
Flyway
GitHub Actions CD
Health Check
CloudWatch
Centralized Logging
```

---

## 26. Conclusion

GarageCare를 AWS EC2 환경에 최초 배포하였다.

Docker Compose를 이용하여 Spring Boot Application과 PostgreSQL을 실행하고,
Production Profile과 환경 변수 기반 Secret 설정을 적용하였다.

또한 실제 외부 Client에서 EC2 Public Network를 통해
GarageCare Login Page에 접근할 수 있음을 확인하였다.

이번 배포를 통해 다음 전체 흐름을 검증하였다.

```text
Source Code
    ↓
Docker Image
    ↓
AWS EC2
    ↓
Docker Compose
    ↓
Spring Boot
    ↓
PostgreSQL
    ↓
External HTTP Request
```

따라서 GarageCare는 로컬 환경에서만 실행되는 프로젝트를 넘어
AWS Infrastructure에서 실행 가능한 Web Application 단계에 도달하였다.

---

## 27. Related

### Previous

```text
Docker Environment
GitHub Actions Build/Test
Production Configuration
```

### Related Files

```text
Dockerfile
compose.yaml
.env.example

src/main/resources/application.properties
src/main/resources/application-dev.properties
src/main/resources/application-prod.properties
```

### Related Documentation

```text
docs/infrastructure/docker.md
docs/infrastructure/ci.md
docs/infrastructure/production-config.md
```

### Next

```text
Production PostgreSQL / RDS
HTTPS / Reverse Proxy
Deployment Automation
Monitoring
```
# Gradle Localhost Connection Error

> Date: 2026-08-14  
> Status: Resolved  
> Area: Gradle / macOS / TCP / Test Environment

---

## Problem

GarageCare의 예약 기능 테스트를 실행하는 과정에서 Gradle이 내부 프로세스와 통신하지 못해 빌드 및 테스트가 실패했다.

처음에는 다음 명령으로 테스트를 실행했다.

```bash
./gradlew clean test --no-daemon
```

하지만 Gradle Daemon 또는 Test Executor가 `localhost(127.0.0.1)`에 연결하지 못하면서 다음 오류가 발생했다.

```text
Could not connect to the Gradle daemon.
```

또는 테스트 실행 단계에서:

```text
org.gradle.internal.remote.internal.ConnectException:
Could not connect to server [... addresses:[/127.0.0.1]]

Caused by:
java.net.BindException: Can't assign requested address
```

---

## Symptoms

오류는 두 가지 형태로 나타났다.

### 1. Gradle Daemon 연결 실패

```text
FAILURE: Build failed with an exception.

* What went wrong:
Could not connect to the Gradle daemon.
```

Daemon 로그에서는 서버 자체는 정상적으로 실행되고 있었다.

```text
Listening on [... addresses:[localhost/127.0.0.1]]

Daemon server started.
```

즉, Gradle Daemon이 실행되지 않는 문제가 아니라
실행된 Daemon에 Gradle Client가 연결하지 못하는 상태였다.

### 2. Test Executor 연결 실패

일부 실행에서는 Gradle Daemon 연결에는 성공하여
빌드가 `:test` 단계까지 진행되었다.

그러나 Gradle Test Executor가 다시 localhost를 통해
Gradle 프로세스와 연결하는 과정에서 동일한 오류가 발생했다.

```text
Could not connect to server
[... port:61922, addresses:[/127.0.0.1]]

Caused by:
java.net.BindException: Can't assign requested address
```

여러 Test Executor가 연속적으로 실패하면서 최종적으로 테스트가 중단되었다.

---

## Investigation

### 1. Java 버전 확인

처음 확인했을 때 기본 Java 버전은 Java 25였다.

```bash
/usr/libexec/java_home -V
```

결과:

```text
25.0.2 OpenJDK
17.0.19 OpenJDK
```

Gradle 및 Spring Boot 프로젝트의 안정적인 실행 환경을 맞추기 위해
Temurin JDK 21을 추가로 설치했다.

```bash
brew install --cask temurin@21
```

이후 Gradle 로그에서 다음과 같이 Java 21 적용을 확인했다.

```text
javaHome=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
javaVersion=21
javaVendor=Eclipse Adoptium
```

그러나 Java 21에서도 동일한 localhost 연결 오류가 발생했다.

따라서 Java 버전 자체가 직접적인 원인은 아니라고 판단했다.

---

### 2. 네트워크 환경 변경

외부 네트워크 문제인지 확인하기 위해 다음 환경에서 각각 테스트했다.

- Wi-Fi
- 스마트폰 Hotspot

두 환경 모두 동일하게 실패했다.

Gradle 내부 통신은 `127.0.0.1`을 사용하고 있었으므로
외부 인터넷 연결 환경보다는 로컬 네트워크 스택 문제일 가능성이 높다고 판단했다.

---

### 3. Loopback Interface 확인

macOS의 loopback interface 상태를 확인했다.

```bash
ifconfig lo0
```

결과:

```text
inet 127.0.0.1
inet6 ::1
```

localhost 통신도 직접 확인했다.

```bash
ping -c 3 127.0.0.1
```

결과:

```text
3 packets transmitted
3 packets received
0.0% packet loss
```

따라서 `lo0` 인터페이스 자체는 정상적으로 동작하고 있었다.

---

### 4. Proxy 확인

macOS Proxy 설정을 확인했다.

```bash
scutil --proxy
```

환경 변수에 Proxy가 설정되어 있는지도 확인했다.

```bash
env | grep -i proxy
```

Gradle localhost 통신을 방해할 만한 Proxy 설정은 발견되지 않았다.

---

### 5. Firewall 확인

macOS Application Firewall 상태를 확인했다.

```bash
/usr/libexec/ApplicationFirewall/socketfilterfw --getglobalstate
```

결과:

```text
Firewall is disabled. (State = 0)
```

따라서 macOS Firewall이 Gradle 내부 통신을 차단하는 문제도 아니었다.

---

### 6. Gradle 설정 확인

프로젝트 및 사용자 전역 Gradle 설정을 확인했다.

```bash
cat gradle.properties
```

```bash
cat ~/.gradle/gradle.properties
```

두 위치 모두 별도의 `gradle.properties`가 존재하지 않았다.

따라서 사용자 정의 Gradle 네트워크 설정에 의해 발생한 문제일 가능성도 낮았다.

---

### 7. Gradle Bind Address 강제 지정

Gradle Daemon이 사용할 주소를 명시적으로 지정해 테스트했다.

```bash
GRADLE_DAEMON_BIND_ADDRESS=127.0.0.1 \
./gradlew clean test --no-daemon
```

실제로 Daemon은 다음과 같이 정상적으로 localhost에서 실행되었다.

```text
Using bind address 127.0.0.1

Listening on
[... port:53430, addresses:[/127.0.0.1]]
```

그러나 Client가 Daemon에 연결하는 과정에서는 여전히 실패했다.

따라서 단순한 Gradle bind address 선택 문제가 아니라고 판단했다.

---

### 8. TCP TIME_WAIT 확인

마지막으로 macOS에 남아 있는 TCP `TIME_WAIT` 연결 수를 확인했다.

```bash
netstat -an -p tcp | grep TIME_WAIT | wc -l
```

결과:

```text
16191
```

약 16,000개의 TCP 연결이 `TIME_WAIT` 상태로 남아 있었다.

이는 당시 시스템에서 매우 많은 TCP 연결이 정리되지 않은 상태였음을 보여주는 중요한 단서였다.

Gradle은 Daemon, Worker, Test Executor 등의 프로세스 간 통신에
localhost TCP socket을 사용한다.

따라서 과도하게 누적된 TCP 연결 상태로 인해 새로운 localhost 연결을 생성하는 과정에서

```text
java.net.BindException:
Can't assign requested address
```

가 발생했을 가능성이 높다고 판단했다.

---

## Root Cause

직접적인 실패 지점은 Gradle 내부 프로세스 사이의
localhost TCP 연결 생성이었다.

```text
Gradle Client
      ↓
127.0.0.1
      ↓
Gradle Daemon

또는

Gradle Test Executor
      ↓
127.0.0.1
      ↓
Gradle Worker
```

Daemon 자체는 정상적으로 localhost에서 listen하고 있었지만,
새 TCP connection을 생성하는 과정에서 운영체제가 다음 오류를 반환했다.

```text
java.net.BindException:
Can't assign requested address
```

Wi-Fi/Hotspot 변경, Java 버전 변경, Proxy, Firewall,
loopback interface 및 Gradle bind address를 확인한 결과
해당 요소들은 직접적인 원인에서 제외할 수 있었다.

최종적으로 약 `16,191`개의 `TIME_WAIT` 연결이 확인되었고,
macOS의 일시적인 TCP/socket resource 상태가 문제의 가장 유력한 원인으로 판단되었다.

> 정확히 어떤 프로세스가 TIME_WAIT를 대량 발생시켰는지는 확인하지 못했으므로,
> 원인을 특정 애플리케이션으로 단정하지 않는다.

---

## Solution

macOS를 재부팅하여 누적되어 있던 네트워크 및 socket 상태를 초기화했다.

재부팅 후 Java 환경을 다시 확인하고 Gradle 테스트를 실행했다.

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"

java -version
```

이후:

```bash
./gradlew clean test --no-daemon
```

Gradle은 더 이상

```text
Can't assign requested address
```

오류로 중단되지 않았고,
Spring Boot 테스트 실행 단계까지 정상적으로 진행되었다.

이후 발생한 오류는 `ApplicationContext` 및 JPA Repository/Schema 관련 오류로,
Gradle localhost 연결 문제와는 별개의 애플리케이션 코드 문제였다.

따라서 Gradle 프로세스 간 연결 문제는 해결된 것으로 판단했다.

---

## Verification

문제 해결 여부는 단순히 Gradle 프로세스가 시작되는지가 아니라
실제로 테스트 단계까지 진입하는지를 기준으로 확인했다.

### Before

```text
Could not connect to the Gradle daemon.

Caused by:
java.net.BindException:
Can't assign requested address
```

또는:

```text
Process 'Gradle Test Executor' finished
with non-zero exit value 1
```

### After

Gradle localhost 연결 오류가 사라지고
Spring Boot `ApplicationContext`가 실제로 실행되었다.

이후 다음과 같은 애플리케이션 레벨 오류가 출력되었다.

```text
Failed to load ApplicationContext
```

이는 Gradle Test Executor가 정상적으로 실행되어
Spring Boot 테스트까지 도달했다는 것을 의미한다.

---

## What I Learned

### 1. `--no-daemon`도 완전히 별도 JVM을 제거하는 것은 아니다

```bash
./gradlew clean test --no-daemon
```

을 실행했음에도 다음 메시지가 나타날 수 있다.

```text
To honour the JVM settings for this build
a single-use Daemon process will be forked.
```

따라서 `--no-daemon`만으로
모든 Gradle 프로세스 간 통신 문제를 우회할 수 있는 것은 아니다.

---

### 2. `localhost` 연결 실패는 인터넷 연결 문제와 다르다

Wi-Fi와 Hotspot 모두에서 동일한 문제가 발생했다.

Gradle이 실패한 주소는 외부 서버가 아니라:

```text
127.0.0.1
```

이었다.

따라서 외부 네트워크보다 먼저
loopback interface와 OS의 TCP/socket 상태를 확인해야 한다.

---

### 3. Stack Trace의 가장 안쪽 `Caused by`가 중요하다

처음에는 다음 오류가 가장 눈에 띄었다.

```text
Could not connect to the Gradle daemon.
```

하지만 실제 핵심 예외는 더 아래에 있었다.

```text
Caused by:
java.net.BindException:
Can't assign requested address
```

상위 예외만 보는 것보다
가장 안쪽의 원인 예외부터 추적하는 것이 중요하다.

---

### 4. 가설을 하나씩 제거하는 방식으로 디버깅한다

이번 문제에서는 다음 순서로 원인 후보를 제거했다.

```text
Java Version
    ↓
External Network
    ↓
Loopback Interface
    ↓
Proxy
    ↓
Firewall
    ↓
Gradle Configuration
    ↓
Bind Address
    ↓
TCP Socket State
```

한 번에 여러 설정을 변경하기보다
각 가설을 검증하면서 원인을 좁혀가는 것이
문제 해결 과정과 결과를 명확하게 만든다.

---

### 5. 환경 문제와 애플리케이션 문제를 분리한다

Gradle 연결 문제가 해결된 이후에는

```text
Failed to load ApplicationContext
```

와 같은 새로운 오류가 발생했다.

이는 기존 문제가 해결되지 않은 것이 아니라,
테스트가 이전보다 더 진행되면서
다음 단계의 애플리케이션 오류가 드러난 것이다.

따라서 오류 메시지가 바뀌었을 때
이전 문제의 연장인지 새로운 문제인지 구분하는 것이 중요하다.

---

## Related

### Issue

- 🚗 Reservation | 예약 등록 기능 구현

### Environment

- macOS
- Java 21 (Eclipse Temurin)
- Spring Boot
- Gradle 9.5.1
- JUnit 5

### Related Commands

```bash
/usr/libexec/java_home -V
ifconfig lo0
ping -c 3 127.0.0.1
scutil --proxy
env | grep -i proxy
/usr/libexec/ApplicationFirewall/socketfilterfw --getglobalstate
netstat -an -p tcp | grep TIME_WAIT | wc -l
./gradlew clean test --no-daemon
```
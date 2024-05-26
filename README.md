# CPU Usage Monitoring System

## Project Setup and Execution

1. Clone Project:
    ```shell
    git clone git@github.com:ChaeYami/cpu-monitoring.git
    ```

2. Database Configuration:

- application.yml 파일 > 데이터베이스 설정 확인:
  ``` yml
  spring:
  datasource:
    url: jdbc:h2:tcp://localhost/~/cpumonitor
    driverClassName: org.h2.Driver
    username: ${H2_USERNAME}
    password: ${H2_PASSWORD}
  ```
- 프로젝트 root 경로 > 환경 변수 파일(.env) 생성 > 데이터베이스 username과 password 추가:
  ```
  H2_USERNAME=
  H2_PASSWORD=
  ```

3. Build and Run with Gradle:
    ```shell
    ./gradlew bootRun
    ```

4. Swagger Documentation:
    ```
    http://localhost:8080/swagger-ui/index.html
    ```


## Requirements

### CPU 사용률 수집 및 DB 저장
- 분 단위로 CPU 사용률을 수집하고 데이터베이스에 저장
- `CpuUsageService` 클래스
1. CPU 사용률 수집 : `CpuUsageCollector` 컴포넌트 클래스에서 실행
    - <details>
      <summary>OperatingSystemMXBean 인터페이스 사용</summary>
      <div markdown = '1'></div>
      
      - `OperatingSystemMXBean`을 서비스 클래스에 주입해서 사용하기 위해 Config에 빈으로 등록
      - 그러나 Spring이 `peratingSystemMXBean`을 MBean으로 등록하려고 하여 문제 발생
          - Java 내부 클래스이기 때문에 MBean으로 등록하지 않아야 함
      - 빈으로 직접 등록하여 사용하는 대신에 별도 컴포넌트 클래스(CpuUsageCollector)로 분리하여 이를 Spring 빈으로 등록해 사용
      </details>

    - `ManagementFactory.getOperatingSystemMXBean()`을 호출하여 현재 JVM의 운영 체제 MXBean을 가져옵니다.
    - <details>
      <summary>System CPU Load 확인:</summary>
      <div markdown = '1'></div>
      
      - `getSystemCpuLoad` 메서드 사용 -> 0.0에서 1.0 사이의 값을 반환하므로 * 100 -> 퍼센트 단위로 변환
      </details>      
    - 에러가 발생할 경우 CPU 사용률을 `null`로 반환하도록 설정
   
2. CPU 사용률 저장 : `saveCpuUsage` 메서드
    - `CpuUsageCollector`클래스의 `collectCpuUsage` 메서드를 실행합니다.
    - 수집한 CPU 사용률이 `null`이 아닐 경우, 소수점 둘째자리까지 반올림 후 현재 시간을 타임스탬프로 설정합니다.
    - CPU 사용률과 현재 시간을 CpuUsage 엔티티에 저장합니다.
    - `cpuUsageRepository.save(usage)`를 호출하여 데이터베이스에 저장합니다.
    
  
### 데이터 조회 API
1. 분 단위 CPU 사용률
   - 지정한 시간 구간의 분 단위 CPU 사용률 조회
   - 구간 시작 시각과 끝 시각을 매개변수로 받아 해당 구간의 데이터를 DB에서 조회해 List로 반환합니다.

3. 시 단위 CPU 사용률
   - 특정 날짜의 시 단위 CPU 사용률 최소/최대/평균 조회
   - 특정 날짜를 매개변수로 받습니다.
   - 해당 날짜의 첫 시각(00:00)과 끝 시각을 사용해 JPA Repository로 DB에서 조회합니다.
   - 타임스탬프의 `getHour`을 사용해 각 데이터의 시간 정보를 추출하고, `stream`의 `collector`로 시간별 그룹핑합니다.
   - 각 시 단위의 최소, 최대값, 평균을 계산해 반환합니다. 

5. 일 단위 CPU 사용률
   - 지정한 구간의 일 단위 CPU 사용률 최소/최대/평균 조회
   - 구간 시작 날짜와 끝 날짜를 매개변수로 받아 해당 구간의 데이터를 DB에서 조회합니다.
   - 끝 날짜의 데이터도 포함해 조회하기 위해 입력받은 끝 날짜에 `endDate.plusDays(1).atStartOfDay().minusNanos(1)` 처리 후 JPA Repository로 DB에서 조회합니다.
   - 타임스탬프의 `toLocalDate`를 사용해 조회한 데이터의 날짜 정보를 추출하고, `collector`로 날짜별 그룹핑합니다.
   - 각 일 단위의 최소, 최대값, 평균을 계산해 반환합니다. 

6. 구간 CPU 사용률 List 의 최소, 최대, 평균값 구하는 메서드
   - 조회한 데이터 List 의 최소, 최대, 평균 구하는 메서드는 반복되므로 분리합니다.
   - 시 단위/일 단위 조회 메서드에서 사용합니다.

### 예외처리 

### 테스트
1. Unit Test

2. Integration Test

   

## API Document

- 분 단위 조회:
    ```
    GET http://127.0.0.1:8080/api/cpumonitoring/minute?startTime=2024-05-26T06:11:00&endTime=2024-05-26T07:11:00
    ```
    - method
    - request
    - response

- 시 단위 조회:
    ```
    GET http://127.0.0.1:8080/api/cpumonitoring/hour?date=2024-05-26
    ```
    - method
    - request
    - response
 
- 일 단위 조회:
    ```
    GET http://127.0.0.1:8080/api/cpumonitoring/minute?startTime=2024-05-26T06:11:00&endTime=2024-05-26T07:11:00
    ```
    - method
    - request
    - response

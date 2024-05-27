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
    ```env
    H2_USERNAME=
    H2_PASSWORD=
    ```

3. Build and Run with Gradle:
    ```shell
    ./gradlew bootRun
    ```

4. Swagger Documentation:
    ```url
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
#### 1. 분 단위 CPU 사용률
   - 지정한 시간 구간의 분 단위 CPU 사용률 조회
   - 구간 시작 시각과 끝 시각을 매개변수로 받아 해당 구간의 데이터를 DB에서 조회해 List로 반환합니다.

#### 2. 시 단위 CPU 사용률
   - 특정 날짜의 시 단위 CPU 사용률 최소/최대/평균 조회
   - 특정 날짜를 매개변수로 받습니다.
   - 해당 날짜의 첫 시각(00:00)과 끝 시각을 사용해 JPA Repository로 DB에서 조회합니다.
   -  <details>
      <summary> 시간별 그룹핑</summary>
      <div markdown = '1'></div>
      
      - 타임스탬프의 `getHour`을 사용해 각 데이터의 시간 정보를 추출하고, `stream`의 `collector`로 시간별 그룹핑합니다.
      </details>
   
   - 각 시 단위의 최소, 최대값, 평균을 계산해 반환합니다. 

#### 3. 일 단위 CPU 사용률
   - 지정한 구간의 일 단위 CPU 사용률 최소/최대/평균 조회   
   - 구간 시작 날짜와 끝 날짜를 매개변수로 받아 해당 구간의 데이터를 DB에서 조회합니다.   
      <details>
      <summary>끝 날짜 처리</summary>
      <div markdown = '1'></div>
      
      - **끝 날짜의 데이터도 포함해 조회**해야 하므로 입력받은 끝 구간보다 + 하루 - 1나노초 처리 후 조회합니다.
      - `endDate.plusDays(1).atStartOfDay().minusNanos(1)` 처리 후 JPA Repository로 DB에서 조회
      </details>
    
   -  <details>
      <summary> 날짜별 그룹핑</summary>
      <div markdown = '1'></div>
      
      - 타임스탬프의 `toLocalDate`를 사용해 조회한 데이터의 날짜 정보를 추출하고, `collector`로 날짜별 그룹핑합니다.
      </details>
   
   - 각 일 단위의 최소, 최대값, 평균을 계산해 반환합니다. 

#### 4. 구간 CPU 사용률 List 의 최소, 최대, 평균값 구하는 메서드 
   - 조회한 데이터 List 의 최소, 최대, 평균 구하는 메서드는 반복되므로 분리합니다.   
   - 시 단위/일 단위 조회 메서드에서 사용합니다.   

#### 5. 데이터 제공 기한
<details>
<summary> 상세보기 </summary>
<div markdown = '1'></div>

- 조회하려는 시간,날짜의 구간 또는 날짜가 제공 기한을 초과할 경우, 자동으로 제공 기한까지 조정 및 조회
- 분 단위 API : 최근 1주 데이터 제공, 일주일 전 날짜의 00시부터
- 시 단위 API : 최근 3달 데이터 제공
- 일 단위 API : 최근 1년 데이터 제공
</details>

<details>
<summary>ex) [분 단위 조회] 최근 1주보다 이전 시각을 시작 구간으로 설정할 경우</summary>
<div markdown = '1'></div>

    
- Query Params : **2023-05-26**T05:00:00, 2024-05-27T18:00:00  
- Request
  ```url
  http://127.0.0.1:8080/api/cpumonitoring/minute?startTime=2023-05-26T05:00:00&endTime=2024-05-27T18:00:00
  ```
- Response
  ```jsonc
    {
        "cpuUsage": [
            {
                "id": 1,
                "usage": "4.67%",
                "timestamp": "2024-05-26T06:11:16"
            },
            {
                "id": 2,
                "usage": "9.41%",
                "timestamp": "2024-05-26T06:12:00"
            },
            {
                "id": 3,
                "usage": "3.31%",
                "timestamp": "2024-05-26T06:13:00"
            },
            {
                "id": 4,
                "usage": "8.56%",
                "timestamp": "2024-05-26T06:14:00"
            },
            // .. 생략 ..
            {
                "id": 58,
                "usage": "13.16%",
                "timestamp": "2024-05-27T16:33:00"
            }
        ],
        "startTime": "2024-05-20T00:00:00", // 자동으로 오늘로부터 일주일 전으로 시작 구간 설정
        "endTime": "2024-05-27T18:00:00"
    }
  ```
</details>
    

### 예외처리 

### 테스트
#### 1. Unit Test
- `CpuUsageServiceTest` : CPU 사용률 수집 및 저장 서비스 계층 단위 테스트
- `CpuMonitoringServiceTest` : 조회 서비스 계층 단위 테스트 (분, 시, 일 단위)

#### 2. Integration Test
- `CpuMonitoringControllerTest` : API 통합 테스트

## API Document

### 분 단위 조회

```url
GET http://127.0.0.1:8080/api/cpumonitoring/minute?startTime=2024-05-26T06:11:00&endTime=2024-05-26T07:11:00
```
- method : GET
- Query Params : startTime, startTime
<details>
<summary>Example</summary>
<div markdown = '1'></div>

- Request
  ```url
  http://127.0.0.1:8080/api/cpumonitoring/minute?startTime=2024-05-26T06:11:00&endTime=2024-05-26T07:11:00
  ```
- Response
  ```json
     {
        "cpuUsage": [
            {
                "id": 1,
                "usage": "4.67%",
                "timestamp": "2024-05-26T06:11:16"
            },
            {
                "id": 2,
                "usage": "9.41%",
                "timestamp": "2024-05-26T06:12:00"
            },
            {
                "id": 3,
                "usage": "3.31%",
                "timestamp": "2024-05-26T06:13:00"
            },
            {
                "id": 4,
                "usage": "8.56%",
                "timestamp": "2024-05-26T06:14:00"
            },
            {
                "id": 5,
                "usage": "8.58%",
                "timestamp": "2024-05-26T06:15:00"
            },
            {
                "id": 6,
                "usage": "5.78%",
                "timestamp": "2024-05-26T06:19:25"
            },
            {
                "id": 7,
                "usage": "21.40%",
                "timestamp": "2024-05-26T06:20:00"
            },
            {
                "id": 8,
                "usage": "14.03%",
                "timestamp": "2024-05-26T06:21:00"
            },
            {
                "id": 9,
                "usage": "9.83%",
                "timestamp": "2024-05-26T06:22:00"
            },
        ],
        "startTime": "2024-05-26T05:00:00",
        "endTime": "2024-05-27T18:00:00"
    }
  ```
</details>
    
    
### 시 단위 조회
```url
GET http://127.0.0.1:8080/api/cpumonitoring/hour?date=2024-05-26
```
- method : GET
- Query Params : date
<details>
<summary>Example</summary>
<div markdown = '1'></div>

- Request
  ```url
  http://127.0.0.1:8080/api/cpumonitoring/hour?date=2024-05-26
  ```
- Response
  ```json
    {
        "cpuUsage": {
            "2024-05-27T02:00": {
                "minUsage": 4.02,
                "maxUsage": 4.02,
                "averageUsage": 4.02
            },
            "2024-05-27T03:00": {
                "minUsage": 4.13,
                "maxUsage": 14.35,
                "averageUsage": 8.06
            },
            "2024-05-27T04:00": {
                "minUsage": 5.06,
                "maxUsage": 77.34,
                "averageUsage": 24.95
            },
            "2024-05-27T05:00": {
                "minUsage": 5.7,
                "maxUsage": 28.67,
                "averageUsage": 17.19
            },
        },
        "date": "2024-05-27"
    }
  ```
</details>

     
### 일 단위 조회
```url
GET http://127.0.0.1:8080/api/cpumonitoring/day?startDate=2024-05-26&endDate=2024-05-27
```
- method : GET
- Query Params : startDate, endDate
<details>
<summary>Example</summary>
<div markdown = '1'></div>

- Request
  ```url
  http://127.0.0.1:8080/api/cpumonitoring/day?startDate=2024-05-26&endDate=2024-05-27
  ```
- Response
  ```json
    {
        "cpuUsage": {
            "2024-05-26": {
                "minUsage": 2.99,
                "maxUsage": 27.69,
                "averageUsage": 9.35
            },
            "2024-05-27": {
                "minUsage": 4.02,
                "maxUsage": 77.34,
                "averageUsage": 14.92
            }
        },
        "startDate": "2024-05-26",
        "endDate": "2024-05-27"
    }
  ```
</details>

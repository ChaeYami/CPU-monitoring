## CPU Usage Monitoring System
> 서버의 CPU 사용률을 분, 시, 주, 월 단위로 모니터링하고 이를 데이터베이스에 저장하여 API를 통해 조회할 수 있는 시스템

## Project Setup and Execution

1. Clone Project:
    ```shell
    git clone git@github.com:ChaeYami/cpu-monitoring.git
    ```

2. Database Configuration:

    > resources 디렉토리 > 데이터베이스 설정 확인:
      
    ``` yml
    # application-dev.yml

    datasource:
        # H2 DB
        url: jdbc:h2:tcp://localhost/~/cpumonitor
        driverClassName: org.h2.Driver
        username: ${H2_USERNAME}
        password: ${H2_PASSWORD}
    ```
    ``` yml
    # application-prod.yml
    
    datasource:
        # mariaDB
        url: jdbc:mariadb://localhost:3307/cpu_monitor?serverTimezone=UTC&characterEncoding=UTF-8
        driverClassName: org.mariadb.jdbc.Driver
        username: ${MARIA_USERNAME}
        password: ${MARIA_PASSWORD}

    ```
    > 프로젝트 root 경로 > 환경 변수 파일(.env) 생성 > 데이터베이스 username과 password 추가:
    ```env
    H2_USERNAME=
    H2_PASSWORD=
    MARIA_USERNAME=
    MARIA_PASSWORD=
    ```

3. Build and Run with Gradle:
    > 개발환경 실행 
    ```shell
    ./gradlew bootRun --args='--spring.profiles.active=dev'
    ```
    > 운용환경 실행
    ```shell
    ./gradlew bootRun --args='--spring.profiles.active=prod'
    ```  

4. Swagger Documentation:
    ```url
    http://localhost:8080/swagger-ui/index.html
    ```

-----

    

## Requirements

### CPU 사용률 수집 및 DB 저장
> 분 단위로 CPU 사용률을 수집하고 데이터베이스에 저장

1. CPU 사용률 수집 : `CpuUsageCollector` 컴포넌트 클래스에서 실행
    - <details>
      <summary>OperatingSystemMXBean 인터페이스 사용</summary>
      <div markdown = '1'></div>
        
      > - `OperatingSystemMXBean`을 서비스 클래스에 주입해서 사용하기 위해 Config에 빈으로 등록
      >     
      > - 그러나 Spring이 `peratingSystemMXBean`을 MBean으로 등록하려고 하여 문제 발생
      >     
      > - 이는 Java 내부 클래스이기 때문에 이미 Java 내부에서 MBean으로 정의되어 있음
      >      
      > - 따라서 별도 컴포넌트 클래스(`CpuUsageCollector`)로 분리하여 이를 Spring 빈으로 등록해 사용   
      
      </details>

    - `ManagementFactory.getOperatingSystemMXBean()`을 호출하여 현재 JVM의 운영 체제 MXBean을 가져옵니다.
    - <details>
      <summary>System CPU Load 확인:</summary>
      <div markdown = '1'></div>
      
      - `getSystemCpuLoad` 메서드 사용 -> 0.0에서 1.0 사이의 값을 반환하므로 * 100 -> 퍼센트 단위로 변환
      </details>      
   
2. CPU 사용률 저장 : `CpuUsageService` 클래스
    - `init` -> `collectInitialCpuUsage` 메서드 : 초기 실행
        - <details>
          <summary> 상세보기 </summary>
          <div markdown = '1'></div>
          
          > - `getSystemCpuLoad` 는 최근 CPU 사용률을 반환
          > 
          > - `OperatingSystemMXBean`이 처음 호출될 때 정확한 CPU 사용률을 제공하지 않아 첫 데이터가 '0%'로 저장되는 경우 발생
          > 
          > - 따라서 서비스 최초 실행 후 DB 저장 전 1초 간격으로 사용률을 5회 수집
          > 
          > - 그 후 실행 당시 사용률 저장 -> 이후 `saveCpuUsage`메서드로 스케쥴러에 따라 수집-저장 실행
          </details>

    - `saveCpuUsage` 메서드 : CPU 사용률 수집 및 저장
        - `CpuUsageCollector`클래스의 `collectCpuUsage` 메서드를 실행합니다.
             
        - 소수점 둘째자리까지 반올림 후 현재 시간을 타임스탬프로 설정합니다.
             
        - CPU 사용률과 현재 시간을 CpuUsage 엔티티에 저장합니다.
             
        - `cpuUsageRepository.save(usage)`를 호출하여 데이터베이스에 저장합니다.
             
    
  
### 데이터 조회 API
#### 1. 분 단위 CPU 사용률
   - 지정한 시간 구간의 분 단위 CPU 사용률 조회
     
   - 구간 시작 시각과 끝 시각을 매개변수로 받아 해당 구간의 데이터를 DB에서 조회해 List로 반환합니다.

   - id값에 따라 정렬 -> 시간 오름차순

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
   - 시간에 대해 오름차순 정렬됩니다.

#### 3. 일 단위 CPU 사용률
   - 지정한 구간의 일 단위 CPU 사용률 최소/최대/평균 조회   
   - 구간 시작 날짜와 끝 날짜를 매개변수로 받아 해당 구간의 데이터를 DB에서 조회합니다.
      - <details>
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
   - 날짜에 대해 오름차순 정렬됩니다.

#### 4. 구간 CPU 사용률 List 의 최소, 최대, 평균값 구하는 메서드 
   - 조회한 데이터 List 의 최소, 최대, 평균 구하는 메서드는 반복되므로 분리합니다.
     
   - 시 단위/일 단위 조회 메서드에서 사용합니다.   

#### 5. 데이터 제공 기한
- 조회하려는 시간(날짜) 범위 또는 날짜가 제공 기한을 초과할 경우

- 시작 시간(날짜)을 자동으로 제공 기한까지 조정 및 조회
-   <details>
    <summary> 제공 기한 </summary>
    <div markdown = '1'></div>
    
        - 분 단위 API : 최근 1주 데이터 제공, (일주일 전 날짜의 00시부터)
        - 시 단위 API : 최근 3달 데이터 제공
        - 일 단위 API : 최근 1년 데이터 제공
    </details>

- ex) 분 단위 조회의 경우
  
    <details>
    <summary> 서비스 메서드 </summary>
    <div markdown = '1'></div>
    
    ``` java
    // 데이터 제공 기한 : 최근 1주 (일주일 전 날짜의 자정으로 설정)
    LocalDateTime providedLimit = LocalDateTime.now().minusWeeks(1).with(LocalTime.MIN);
    // 구간이 기한 초과시 자동 조절
    startTime = startTime.isBefore(providedLimit) ? providedLimit : startTime;
    ```
    </details>
    
    <details>
    <summary>[API] 최근 1주보다 이전 시각을 시작 구간으로 설정할 경우</summary>
    <div markdown = '1'></div>
      
    - Query Params :
        - startTime : **2023-05-26**T05:00:00,
        - endTime : 2024-05-27T18:00:00
      
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
#### 1. 데이터 수집 시 예외처리
- CPU 사용률을 수집하고 저장하는 메서드(`collectCpuUsage`, `saveCpuUsage`) 실행 시 에러가 발생하면 커스텀 예외를 던지도록 설정
  
- 해당 커스텀 예외에서 메시지에 에러 내용 반환
  
- `ExceptionHandler`에서 커스텀 예외 발생 시 에러 로그로 에러 메시지를 남기도록 설정

- 기타 예외 상황에도 `ExceptionHandler` 적용

#### 2. API 요청 시 잘못된 파라미터에 대한 예외

- 메서드별 파라미터 형식 에러(날짜, 시간) : `MethodArgumentTypeMismatchException` 사용

- 컨트롤러에서 해당 에러 발생 시 'Invalid parameter' 메시지와 함께 HTTP `BAD_REQUEST` 상태코드 반환

- 구간 시작 날짜(또는 시간)가 종료 날짜(또는 시간)보다 뒤인 경우
  
   - 커스텀 에러 : `InvalidDateTimeRangeException` 에러 처리


### 테스트
#### 1. Unit Test
- `CpuUsageServiceTest` : CPU 사용률 수집 및 저장 서비스 계층 단위 테스트
  
- `CpuMonitoringServiceTest` : 조회 서비스 계층 단위 테스트 (분, 시, 일 단위)

#### 2. Integration Test
- `CpuMonitoringControllerTest` : API 통합 테스트

-----

## API Document

### 분 단위 조회

```url
GET http://127.0.0.1:8080/api/cpumonitoring/minute?startTime=2024-05-26T06:11:00&endTime=2024-05-26T07:11:00
```
- **method : GET**
- **Query Params : startTime, startTime**
<details>
<summary>Example : Success</summary>
<div markdown = '1'></div>

- Request
  
  ```url
  http://127.0.0.1:8080/api/cpumonitoring/minute?startTime=2024-05-26T06:11:00&endTime=2024-05-26T07:11:00
  ```
- Response

  > Status Code : 200 OK
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

<details>
<summary>Example : Invalid parameter</summary>
<div markdown = '1'></div>

- Request
  
    > startTime : 2024-05-27 -> DateTime 파라미터에 Date 넣어 요청 
  ```url
  http://127.0.0.1:8080/api/cpumonitoring/minute?startTime=2024-05-27&endTime=2024-05-27T23:50:00
  ```
- Response

  > Status Code : 400 Bad Request
  ```
  Invalid parameter: startTime
  ```
</details>

<details>
<summary>Example : Invalid DateTimeRange</summary>
<div markdown = '1'></div>

- Request
  
    > startTime : 2024-05-28T01:30:00   
    > endTime : 2024-05-28T01:00:00   
    > -> 시작 시간이 종료 시간보다 뒤인 경우   
  ```url
  http://127.0.0.1:8080/api/cpumonitoring/minute?startTime=2024-05-28T01:30:00&endTime=2024-05-28T01:00:00
  ```
- Response

  > Status Code : 400 Bad Request
  ```
  startDate(startTime) cannot be after endDate(endTime)
  ```
</details>
    
    
### 시 단위 조회
```url
GET http://127.0.0.1:8080/api/cpumonitoring/hour?date=2024-05-26
```
- **method : GET**
- **Query Params : date**
<details>
<summary>Example : Success</summary>
<div markdown = '1'></div>

- Request
  
  ```url
  http://127.0.0.1:8080/api/cpumonitoring/hour?date=2024-05-26
  ```
- Response

  > Status Code : 200 OK
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

<details>
<summary>Example : Invalid parameter</summary>
<div markdown = '1'></div>

- Request
  
    > date : 2024 -> Date 형식 불일치 
  ```url
  http://127.0.0.1:8080/api/cpumonitoring/hour?date=2024
  ```
- Response

  > Status Code : 400 Bad Request
  ```
  Invalid parameter: date
  ```
</details>
     
### 일 단위 조회
```url
GET http://127.0.0.1:8080/api/cpumonitoring/day?startDate=2024-05-26&endDate=2024-05-27
```
- **method : GET**
- **Query Params : startDate, endDate**
<details>
<summary>Example : Success</summary>
<div markdown = '1'></div>

- Request
  
  ```url
  http://127.0.0.1:8080/api/cpumonitoring/day?startDate=2024-05-26&endDate=2024-05-27
  ```
- Response

  > Status Code : 200 OK
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

<details>
<summary>Example : Invalid parameter</summary>
<div markdown = '1'></div>

- Request
  
    > startDate : 2022 -> Date 형식 불일치 
  ```url
  http://127.0.0.1:8080/api/cpumonitoring/day?startDate=2022&endDate=2024-05-27
  ```
- Response

  > Status Code : 400 Bad Request
  ```
  Invalid parameter: startDate
  ```
</details>

<details>
<summary>Example : Invalid DateTimeRange</summary>
<div markdown = '1'></div>

- Request
  
    > startDate : 2024-05-28   
    > endDate : 2024-05-27   
    > -> 시작 날짜가 종료 날짜보다 뒤인 경우   
  ```url
  http://127.0.0.1:8080/api/cpumonitoring/day?startDate=2024-05-28&endDate=2024-05-27
  ```
- Response

  > Status Code : 400 Bad Request
  ```
  startDate(startTime) cannot be after endDate(endTime)
  ```
</details>

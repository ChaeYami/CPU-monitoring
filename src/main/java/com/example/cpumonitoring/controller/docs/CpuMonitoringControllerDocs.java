package com.example.cpumonitoring.controller.docs;

import com.example.cpumonitoring.model.CpuUsageDateResponse;
import com.example.cpumonitoring.model.CpuUsageHourResponse;
import com.example.cpumonitoring.model.CpuUsageMinuteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Tag(name = "CPU Monitoring API", description = "API for monitoring CPU usage")
public interface CpuMonitoringControllerDocs {

    @Operation(summary = "Get CPU Usage by Minute", description = "지정한 시간 구간의 분 단위 CPU 사용률 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success", content = {
                    @Content(mediaType = "application/json", examples = {
                            @ExampleObject(
                                    value = "{" +
                                            "\"cpuUsage\": [" +
                                            "{\"id\": 1, \"usage\": \"4.67%\", \"timestamp\": \"2024-05-26T06:11:16\"}," +
                                            "{\"id\": 2, \"usage\": \"9.41%\", \"timestamp\": \"2024-05-26T06:12:00\"}," +
                                            "{\"id\": 3, \"usage\": \"3.31%\", \"timestamp\": \"2024-05-26T06:13:00\"}," +
                                            "{\"id\": 4, \"usage\": \"8.56%\", \"timestamp\": \"2024-05-26T06:14:00\"}," +
                                            "{\"id\": 5, \"usage\": \"8.58%\", \"timestamp\": \"2024-05-26T06:15:00\"}," +
                                            "{\"id\": 6, \"usage\": \"5.78%\", \"timestamp\": \"2024-05-26T06:19:25\"}," +
                                            "{\"id\": 7, \"usage\": \"21.40%\", \"timestamp\": \"2024-05-26T06:20:00\"}," +
                                            "{\"id\": 8, \"usage\": \"14.03%\", \"timestamp\": \"2024-05-26T06:21:00\"}," +
                                            "{\"id\": 9, \"usage\": \"9.83%\", \"timestamp\": \"2024-05-26T06:22:00\"}" +
                                            "]," +
                                            "\"startTime\": \"2024-05-26T05:00:00\"," +
                                            "\"endTime\": \"2024-05-27T18:00:00\"" +
                                            "}"
                            )
                    })
            }),
            @ApiResponse(responseCode = "400", description = "실패 : 400 Bad Request", content = {
                    @Content(mediaType = "application/json", examples = {
                            @ExampleObject(
                                    name = "잘못된 파라미터",
                                    value = "{\"message\": \"Invalid parameter: startTime\", \"startTime\": \"2024-05-27\", \"endTime\": \"2023-12-31T23:59:59\"}"
                            ),
                            @ExampleObject(
                                    name = "시작 시간이 종료 시간보다 뒤인 경우",
                                    value = "{\"message\": \"startDate(startTime) cannot be after endDate(endTime)\", \"startTime\": \"2024-05-28T01:30:00\", \"endTime\": \"2024-05-28T01:00:00\"}"
                            ),
                            @ExampleObject(
                                    name = "종료 시간이 현재보다 뒤인 경우",
                                    value = "{\"message\": \"endTime after current time cannot be specified.\", \"startTime\": \"2024-05-28T01:30:00\", \"endTime\": \"2024-07-28T01:00:00\"}"
                            )
                    }),
            })
    })
    @Parameters({
            @Parameter(name = "startTime", description = "구간 시작 시각", example = "2024-05-20T00:00:00"),
            @Parameter(name = "endTime", description = "구간 종료 시각", example = "2024-05-20T00:10:00")
    })
    @GetMapping("/minute")
    ResponseEntity<CpuUsageMinuteResponse> getCpuUsageByMinute(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime);

    @Operation(summary = "Get CPU Usage by Hour", description = "특정 날짜의 시 단위 CPU 사용률 최소/최대/평균 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success", content = {
                    @Content(mediaType = "application/json", examples = {
                            @ExampleObject(
                                    value = "{" +
                                            "\"cpuUsage\": {" +
                                            "\"2024-05-27T02:00\": {" +
                                            "\"minUsage\": 4.02," +
                                            "\"maxUsage\": 4.02," +
                                            "\"averageUsage\": 4.02" +
                                            "}," +
                                            "\"2024-05-27T03:00\": {" +
                                            "\"minUsage\": 4.13," +
                                            "\"maxUsage\": 14.35," +
                                            "\"averageUsage\": 8.06" +
                                            "}," +
                                            "\"2024-05-27T04:00\": {" +
                                            "\"minUsage\": 5.06," +
                                            "\"maxUsage\": 77.34," +
                                            "\"averageUsage\": 24.95" +
                                            "}," +
                                            "\"2024-05-27T05:00\": {" +
                                            "\"minUsage\": 5.7," +
                                            "\"maxUsage\": 28.67," +
                                            "\"averageUsage\": 17.19" +
                                            "}" +
                                            "}," +
                                            "\"date\": \"2024-05-27\"" +
                                            "}"
                            )
                    })
            }),
            @ApiResponse(responseCode = "400", description = "실패 : 400 Bad Request", content = {
                    @Content(mediaType = "application/json", examples = {
                            @ExampleObject(
                                    name = "잘못된 파라미터",
                                    value = "{\"message\": \"Invalid parameter: date\", \"date\": \"2024\"}"
                            ),
                            @ExampleObject(
                                    name = "날짜가 오늘보다 뒤인 경우",
                                    value = "{\"message\": \"date after today cannot be specified.\", \"date \": \"2024-08-28\"}"
                            ),
                    }),
            })
    })
    @Parameters({
            @Parameter(name = "date", description = "날짜", example = "2024-05-20")
    })
    @GetMapping("/hour")
    ResponseEntity<CpuUsageHourResponse> getCpuUsageStatsByHour(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date);

    @Operation(summary = "Get CPU Usage by Day", description = "지정한 구간의 일 단위 CPU 사용률 최소/최대/평균 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success", content = {
                    @Content(mediaType = "application/json",examples = {
                            @ExampleObject(
                                    value = "{" +
                                            "\"cpuUsage\": {" +
                                            "\"2024-05-26\": {" +
                                            "\"minUsage\": 2.99," +
                                            "\"maxUsage\": 27.69," +
                                            "\"averageUsage\": 9.35" +
                                            "}," +
                                            "\"2024-05-27\": {" +
                                            "\"minUsage\": 4.02," +
                                            "\"maxUsage\": 77.34," +
                                            "\"averageUsage\": 14.92" +
                                            "}" +
                                            "}," +
                                            "\"startDate\": \"2024-05-26\"," +
                                            "\"endDate\": \"2024-05-27\"" +
                                            "}"
                            )
                    })
            }),
            @ApiResponse(responseCode = "400", description = "실패 : 400 Bad Request", content = {
                    @Content(mediaType = "application/json", examples = {
                            @ExampleObject(
                                    name = "잘못된 파라미터",
                                    value = "{\"message\": \"Invalid parameter: startDate\", \"startDate \": \"2022\", \"endDate\": \"2023-12-31\"}"
                            ),
                            @ExampleObject(
                                    name = "시작 날짜가 종료 날짜보다 뒤인 경우",
                                    value = "{\"message\": \"startDate(startTime) cannot be after endDate(endDate)\", \"startDate\": \"2024-05-28\", \"endDate\": \"2024-05-27\"}"
                            ),
                            @ExampleObject(
                                    name = "종료 날짜가 오늘보다 뒤인 경우",
                                    value = "{\"message\": \"endDate after today cannot be specified.\", \"startDate\": \"2020-05-27\", \"endDate\": \" 2024-07-29\"}"
                            )
                    }),
            })
    })
    @Parameters({
            @Parameter(name = "startDate", description = "구간 시작 날짜", example = "2024-05-20"),
            @Parameter(name = "endDate", description = "End date of the range", example = "2024-05-21")
    })
    @GetMapping("/day")
    ResponseEntity<CpuUsageDateResponse> getCpuUsageStatsByDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);
}
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
            @ApiResponse(responseCode = "200", description = "success", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "실패 : 잘못된 파라미터", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Invalid parameter: startTime\"}"))),
            @ApiResponse(responseCode = "400", description = "실패 : 시작 시간이 종료 시간보다 뒤인 겨우", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"startDate(startTime) cannot be after endDate(endTime)\"}"))),
            @ApiResponse(responseCode = "400", description = "실패 : 종료 시간이 현재보다 뒤인 경우", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"endTime after current time cannot be specified.\"}")))
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
            @ApiResponse(responseCode = "200", description = "success", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "실패 : 잘못된 파라미터", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Invalid parameter: date\"}"))),
            @ApiResponse(responseCode = "400", description = "실패 : 날짜가 오늘보다 뒤인 경우", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"date after today cannot be specified.\"}")))
    })
    @Parameters({
            @Parameter(name = "date", description = "날짜", example = "2024-05-20")
    })
    @GetMapping("/hour")
    ResponseEntity<CpuUsageHourResponse> getCpuUsageStatsByHour(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date);

    @Operation(summary = "Get CPU Usage by Day", description = "지정한 구간의 일 단위 CPU 사용률 최소/최대/평균 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "실패 : 잘못된 파라미터", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Invalid parameter: startDate\"}"))),
            @ApiResponse(responseCode = "400", description = "실패 : 시작 날짜가 종료 날짜보다 뒤인 경우", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"startDate(startTime) cannot be after endDate(endTime)\"}"))),
            @ApiResponse(responseCode = "400", description = "실패 : 종료 날짜가 오늘보다 뒤인 경우", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"endDate after today cannot be specified.\"}")))
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
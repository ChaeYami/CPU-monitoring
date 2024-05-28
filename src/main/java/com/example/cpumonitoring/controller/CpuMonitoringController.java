package com.example.cpumonitoring.controller;

import com.example.cpumonitoring.controller.docs.CpuMonitoringControllerDocs;
import com.example.cpumonitoring.model.CpuUsageDateResponse;
import com.example.cpumonitoring.model.CpuUsageMinuteResponse;
import com.example.cpumonitoring.model.CpuUsageHourResponse;
import com.example.cpumonitoring.service.CpuMonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cpumonitoring")
public class CpuMonitoringController implements CpuMonitoringControllerDocs {

    private final CpuMonitoringService cpuMonitoringService;

    /**
     * 지정한 시간 구간의 분 단위 CPU 사용률 조회
     * @param startTime 구간 시작 시각
     * @param endTime 구간 끝 시각
     * @return 사용률 List
     */
    @GetMapping("/minute")
    public ResponseEntity<CpuUsageMinuteResponse> getCpuUsageByMinute(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        CpuUsageMinuteResponse cpuUsages = cpuMonitoringService.getCpuUsageByMinute(startTime, endTime);
        return ResponseEntity.ok(cpuUsages);
    }

    /**
     * 특정 날짜의 시 단위 CPU 사용률 최소/최대/평균 조회
     * @param date 조회할 날짜
     * @return 사용률 List
     */
    @GetMapping("/hour")
    public ResponseEntity<CpuUsageHourResponse> getCpuUsageStatsByHour(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        CpuUsageHourResponse cpuUsageStatsByHour = cpuMonitoringService.getCpuUsageStatsByHour(startDate, endDate);
        return ResponseEntity.ok(cpuUsageStatsByHour);
    }

    /**
     * 지정한 구간의 일 단위 CPU 사용률 최소/최대/평균 조회
     * @param startDate 구간 시작 날짜
     * @param endDate 구간 끝 날짜
     * @return 사용률 List
     */
    @GetMapping("/day")
    public ResponseEntity<CpuUsageDateResponse> getCpuUsageStatsByDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        CpuUsageDateResponse cpuUsageStatsByDay = cpuMonitoringService.getCpuUsageStatsByDay(startDate, endDate);
        return ResponseEntity.ok(cpuUsageStatsByDay);
    }

}
package com.example.cpumonitoring.model;

import com.example.cpumonitoring.entity.CpuUsage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@AllArgsConstructor
@Getter
public class CpuUsageHourResponse {
    private Map<LocalDateTime, CpuUsageStats> cpuUsage;
    private LocalDate date;

}
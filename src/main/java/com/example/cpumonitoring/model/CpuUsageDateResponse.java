package com.example.cpumonitoring.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;

@Data
@AllArgsConstructor
@Getter
public class CpuUsageDateResponse {
    private Map<LocalDate, CpuUsageStats> cpuUsage;
    private LocalDate startDate;
    private LocalDate endDate;


}
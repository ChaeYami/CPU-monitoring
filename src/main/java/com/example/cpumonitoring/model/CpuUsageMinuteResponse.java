package com.example.cpumonitoring.model;

import com.example.cpumonitoring.entity.CpuUsage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;


@AllArgsConstructor
@Getter
public class CpuUsageMinuteResponse {
    private List<CpuUsage> cpuUsage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}
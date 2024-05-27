package com.example.cpumonitoring.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;


@AllArgsConstructor
@Getter
public class CpuUsageStats {
    private double minUsage;
    private double maxUsage;
    private double averageUsage;

}
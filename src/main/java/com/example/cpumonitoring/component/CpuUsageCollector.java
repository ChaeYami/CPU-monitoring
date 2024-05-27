package com.example.cpumonitoring.component;

import com.example.cpumonitoring.exception.CpuUsageCollectionException;
import com.sun.management.OperatingSystemMXBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;

@Slf4j
@Component
public class CpuUsageCollector {
    private final OperatingSystemMXBean operatingSystemMXBean;

    public CpuUsageCollector() {
        this.operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    public Double collectCpuUsage() {
        try {
            return operatingSystemMXBean.getSystemCpuLoad() * 100;
        } catch (Exception e) {
            throw new CpuUsageCollectionException("Failed to collect CPU usage data", e);
        }
    }
}
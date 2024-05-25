package com.example.cpumonitoring.service;

import com.example.cpumonitoring.entity.CpuUsage;
import com.example.cpumonitoring.repository.CpuUsageRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CpuMonitoringService {
    private final CpuUsageRepository cpuUsageRepository;

    @PostConstruct
    public void init() {
        // 초기화 시점에서 일정 시간 동안 CPU 사용률을 미리 수집
        try {
            for (int i = 0; i < 5; i++) {
                getCpuUsage();
                Thread.sleep(1000); // 1초 대기
            }
            saveCpuUsage();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    //스케쥴러 사용 - 가져온 cpu 사용률 DB에 저장
    @Scheduled(cron = "0/10 * * * * *") // 매 10초마다 실행
    public void saveCpuUsage() {
        double cpuUsage = getCpuUsage(); // CPU 사용률 수집 로직
        // 소수점 셋째자리에서 반올림 후 % ex) 7.93%
        String formattedCpuUsage = formatCpuUsage(cpuUsage);

        CpuUsage usage = new CpuUsage(formattedCpuUsage, LocalDateTime.now().withNano(0));

        cpuUsageRepository.save(usage);
    }

    // cpu 사용률 - 소수점 셋째자리에서 반올림 후 % ex) 7.93%
    private String formatCpuUsage(double cpuUsage) {
        BigDecimal newUsage = new BigDecimal(cpuUsage).setScale(2, RoundingMode.HALF_UP);
        return newUsage.toString() + "%";
    }

    // CPU 사용률 가져오는 로직
    private double getCpuUsage() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
            return sunOsBean.getSystemCpuLoad() * 100;
        } else {
            throw new UnsupportedOperationException("CPU usage measurement not supported on this platform.");
        }
    }

}
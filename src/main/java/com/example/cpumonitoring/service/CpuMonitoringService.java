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
        try {
            Double cpuUsage = getCpuUsage(); // CPU 사용률 수집 로직
            if (cpuUsage != null) {
                // 소수점 셋째자리에서 반올림 후 % ex) 7.93%
                String formattedCpuUsage = formatCpuUsage(cpuUsage);

                CpuUsage usage = new CpuUsage(formattedCpuUsage, LocalDateTime.now().withNano(0));

                cpuUsageRepository.save(usage);
            }
        } catch (Exception e) {
            log.error("Failed to save CPU usage data : {}", e.getMessage());
        }
    }

    // cpu 사용률 - 소수점 셋째자리에서 반올림 후 % ex) 7.93%
    private String formatCpuUsage(double cpuUsage) {
        BigDecimal newUsage = new BigDecimal(cpuUsage).setScale(2, RoundingMode.HALF_UP);
        return newUsage.toString() + "%";
    }

    // CPU 사용률 가져오는 로직
    private Double getCpuUsage() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
            return sunOsBean.getSystemCpuLoad() * 100;
        } catch (Exception e) {
            log.error("Failed to get CPU usage Data : {}", e.getMessage());
            return null;
        }

    }

    // TODO : 지정한 시간 구간의 분 단위 CPU 사용률 조회

    // TODO : 지정한 날짜의 시 단위 CPU 최소/최대/평균 사용률 조회

    // TODO : 지정한 날짜 구간의 일 단위 CPU 최소/최대/평균 사용률 조회

}
package com.example.cpumonitoring.service;

import com.example.cpumonitoring.entity.CpuUsage;
import com.example.cpumonitoring.model.CpuUsageStats;
import com.example.cpumonitoring.repository.CpuUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CpuMonitoringService {
    private final CpuUsageRepository cpuUsageRepository;

    /**
     * 지정한 시간 구간의 분 단위 CPU 사용률 조회
     * @param startTime 구간 시작 시각
     * @param endTime 구간 끝 시각
     * @return 사용률 List
     */
    public List<CpuUsage> getCpuUsageByMinute(LocalDateTime startTime, LocalDateTime endTime) {
        return cpuUsageRepository.findByTimestampBetween(startTime, endTime);
    }

    /**
     * 특정 날짜의 시 단위 CPU 사용률 최소/최대/평균 조회
     * @param date 조회할 날짜
     * @return 사용률 List
     */
    public Map<Integer, CpuUsageStats> getCpuUsageStatsByHour(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<CpuUsage> cpuUsages = cpuUsageRepository.findByTimestampBetween(startOfDay, endOfDay);

        return cpuUsages.stream()
                .collect(Collectors.groupingBy(
                        usage -> usage.getTimestamp().getHour(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                this::calculateUsageStats
                        )
                ));
    }

    /**
     * 지정한 구간의 일 단위 CPU 사용률 최소/최대/평균 조회
     * @param startDate 구간 시작 날짜
     * @param endDate 구간 끝 날짜
     * @return 사용률 List
     */
    public Map<LocalDate, CpuUsageStats> getCpuUsageStatsByDay(LocalDate startDate, LocalDate endDate) {
        List<CpuUsage> cpuUsages = cpuUsageRepository.findByTimestampBetween(
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay().minusNanos(1)
        );

        return cpuUsages.stream()
                .collect(Collectors.groupingBy(
                        usage -> usage.getTimestamp().toLocalDate(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                this::calculateUsageStats
                        )
                ));
    }

    // 구간 CPU 사용률 List 의 최소, 최대, 평균값 구하는 메서드
    private CpuUsageStats calculateUsageStats(List<CpuUsage> usageList) {
        double minUsage = usageList.stream()
                .mapToDouble(usage -> parseUsage(usage.getUsage()))
                .min()
                .orElse(0);
        double maxUsage = usageList.stream()
                .mapToDouble(usage -> parseUsage(usage.getUsage()))
                .max()
                .orElse(0);
        double averageUsage = usageList.stream()
                .mapToDouble(usage -> parseUsage(usage.getUsage()))
                .average()
                .orElse(0);
        averageUsage = Math.round(averageUsage * 100.0) / 100.0;
        return new CpuUsageStats(minUsage, maxUsage, averageUsage);
    }

    private double parseUsage(String usage) {
        return Double.parseDouble(usage.replace("%", ""));
    }

}
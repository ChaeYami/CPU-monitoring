package com.example.cpumonitoring.service;

import com.example.cpumonitoring.entity.CpuUsage;
import com.example.cpumonitoring.model.CpuUsageDateResponse;
import com.example.cpumonitoring.model.CpuUsageMinuteResponse;
import com.example.cpumonitoring.model.CpuUsageHourResponse;
import com.example.cpumonitoring.model.CpuUsageStats;
import com.example.cpumonitoring.repository.CpuUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.TreeMap;

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
    public CpuUsageMinuteResponse getCpuUsageByMinute(LocalDateTime startTime, LocalDateTime endTime) {
        // 데이터 제공 기한 : 최근 1주 (일주일 전 날짜의 자정으로 설정)
        LocalDateTime providedLimit = LocalDateTime.now().minusWeeks(1).with(LocalTime.MIN);
        if (startTime.isBefore(providedLimit)) { // 시작 구간이 기한을 초과할 경우
            startTime = providedLimit; // 시작 구간을 일주일 전으로 자동 조정
        }
        List<CpuUsage> cpuUsages = cpuUsageRepository.findByTimestampBetween(startTime, endTime);

        return new CpuUsageMinuteResponse(cpuUsages, startTime, endTime);
    }

    /**
     * 특정 날짜의 시 단위 CPU 사용률 최소/최대/평균 조회
     * @param date 조회할 날짜
     * @return 사용률 List
     */
    public CpuUsageHourResponse getCpuUsageStatsByHour(LocalDate date) {
        // 데이터 제공 기한 : 최근 3달
        LocalDate providedLimit = LocalDate.now().minus(3, ChronoUnit.MONTHS);
        if (date.isBefore(providedLimit)) {
            date = providedLimit;
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<CpuUsage> cpuUsages = cpuUsageRepository.findByTimestampBetween(startOfDay, endOfDay);

        Map<LocalDateTime, CpuUsageStats> returnCpuUsages = cpuUsages.stream()
                .collect(Collectors.groupingBy(
                        usage -> usage.getTimestamp()
                                .withMinute(0)
                                .withSecond(0)
                                .withNano(0),
                        TreeMap::new, // 시간 오름차순 정렬
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                this::calculateUsageStats
                        )
                ));
        return new CpuUsageHourResponse(returnCpuUsages, date);
    }

    /**
     * 지정한 구간의 일 단위 CPU 사용률 최소/최대/평균 조회
     * @param startDate 구간 시작 날짜
     * @param endDate 구간 끝 날짜
     * @return 사용률 List
     */
    public CpuUsageDateResponse getCpuUsageStatsByDay(LocalDate startDate, LocalDate endDate) {
        // 데이터 제공 기한 : 최근 1년
        LocalDate providedLimit = LocalDate.now().minusYears(1);

        if (startDate.isBefore(providedLimit)) {
            startDate = providedLimit;
        }

        List<CpuUsage> cpuUsages = cpuUsageRepository.findByTimestampBetween(
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay().minusNanos(1)
        );

        Map<LocalDate, CpuUsageStats> returnCpuUsages = cpuUsages.stream()
                .collect(Collectors.groupingBy(
                        usage -> usage.getTimestamp().toLocalDate(),
                        TreeMap::new, // 날짜 정렬
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                this::calculateUsageStats
                        )
                ));

        return new CpuUsageDateResponse(returnCpuUsages, startDate, endDate);
    }

    // 구간 CPU 사용률 List 의 최소, 최대, 평균값 구하는 메서드
    private CpuUsageStats calculateUsageStats(List<CpuUsage> usageList) {
        double minUsage = usageList.stream()
                .mapToDouble(usage -> parseUsage(usage.getCpuUsage()))
                .min()
                .orElse(0);
        double maxUsage = usageList.stream()
                .mapToDouble(usage -> parseUsage(usage.getCpuUsage()))
                .max()
                .orElse(0);
        double averageUsage = usageList.stream()
                .mapToDouble(usage -> parseUsage(usage.getCpuUsage()))
                .average()
                .orElse(0);
        averageUsage = Math.round(averageUsage * 100.0) / 100.0;
        return new CpuUsageStats(minUsage, maxUsage, averageUsage);
    }

    private double parseUsage(String usage) {
        return Double.parseDouble(usage.replace("%", ""));
    }

}
package com.example.cpumonitoring.service;

import com.example.cpumonitoring.entity.CpuUsage;
import com.example.cpumonitoring.exception.InvalidDateTimeRangeException;
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
import java.time.temporal.TemporalUnit;
import java.util.DoubleSummaryStatistics;
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
        // 시작 시간이 종료 시간보다 뒷 시간인 경우
        if (startTime.isAfter(endTime)) {
            throw new InvalidDateTimeRangeException();
        }
        // 종료 시간이 현재보다 뒤인 경우
        if (endTime.isAfter(LocalDateTime.now())) {
            throw new InvalidDateTimeRangeException("endTime after current time cannot be specified.");
        }
        // 데이터 제공 기한 : 최근 1주 (일주일 전 날짜의 자정으로 설정)
        LocalDateTime providedLimit = LocalDateTime.now().minusWeeks(1).with(LocalTime.MIN);

        // 구간이 기한 초과시 자동 조절
        startTime = startTime.isBefore(providedLimit) ? providedLimit : startTime;

        List<CpuUsage> cpuUsages = cpuUsageRepository.findByTimestampBetween(startTime, endTime);

        return new CpuUsageMinuteResponse(cpuUsages, startTime, endTime);
    }

    /**
     * 특정 날짜의 시 단위 CPU 사용률 최소/최대/평균 조회
     * @param date 조회할 날짜
     * @return 사용률 List
     */
    public CpuUsageHourResponse getCpuUsageStatsByHour(LocalDate startDate , LocalDate endDate) {
        LocalDate adjustedStartDate = adjustDate(startDate, 3, ChronoUnit.MONTHS);

        validateDateRange(startDate, endDate);

        LocalDateTime startOfDay = adjustedStartDate.atStartOfDay();
        LocalDateTime endOfDay = endDate.atTime(LocalTime.MAX);

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
        return new CpuUsageHourResponse(returnCpuUsages, adjustedStartDate,endDate);
    }

    /**
     * 지정한 구간의 일 단위 CPU 사용률 최소/최대/평균 조회
     * @param startDate 구간 시작 날짜
     * @param endDate 구간 끝 날짜
     * @return 사용률 List
     */
    public CpuUsageDateResponse getCpuUsageStatsByDay(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        LocalDate adjustedStartDate = adjustDate(startDate, 1, ChronoUnit.YEARS);

        List<CpuUsage> cpuUsages = cpuUsageRepository.findByTimestampBetween(
                adjustedStartDate.atStartOfDay(),
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

        return new CpuUsageDateResponse(returnCpuUsages, adjustedStartDate, endDate);
    }

    private static void validateDateRange(LocalDate startDate, LocalDate endDate) {
        // 시작 날짜가 종료 날짜보다 뒤인 경우
        if (startDate.isAfter(endDate)) {
            throw new InvalidDateTimeRangeException();
        }
        // 종료 날짜가 오늘보다 뒤인 경우
        if (endDate.isAfter(LocalDate.now())) {
            throw new InvalidDateTimeRangeException("endDate after today cannot be specified.");
        }
    }

    // 제공기한 초과시 자동 조절하는 메서드
    public LocalDate adjustDate(LocalDate date, int limit, TemporalUnit unit) {
        LocalDate providedLimit = LocalDate.now().minus(limit, unit);
        // 날짜가 기한 초과시 자동 조절
        date = date.isBefore(providedLimit) ? providedLimit : date;

        return date;
    }

    // 구간 CPU 사용률 List 의 최소, 최대, 평균값 구하는 메서드
    private CpuUsageStats calculateUsageStats(List<CpuUsage> usageList) {
        DoubleSummaryStatistics stats = usageList.stream()
                .mapToDouble(usage -> usage.getCpuUsage())
                .summaryStatistics();
        double minUsage = stats.getMin();
        double maxUsage = stats.getMax();
        double averageUsage = Math.round(stats.getAverage() * 100.0) / 100.0;
        return new CpuUsageStats(minUsage, maxUsage, averageUsage);
    }

}
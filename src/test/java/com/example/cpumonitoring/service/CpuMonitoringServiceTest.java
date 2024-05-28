package com.example.cpumonitoring.service;

import com.example.cpumonitoring.entity.CpuUsage;
import com.example.cpumonitoring.model.CpuUsageStats;
import com.example.cpumonitoring.repository.CpuUsageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CpuMonitoringServiceTest {

    @Mock
    private CpuUsageRepository cpuUsageRepository;

    @InjectMocks
    private CpuMonitoringService cpuMonitoringService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("분 단위 조회")
    void testGetCpuUsageByMinute() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(30);
        LocalDateTime endTime = LocalDateTime.now();
        List<CpuUsage> fakeData = Arrays.asList(
                new CpuUsage(50, LocalDateTime.now().minusMinutes(20)),
                new CpuUsage(60, LocalDateTime.now().minusMinutes(15)),
                new CpuUsage(70, LocalDateTime.now().minusMinutes(10))
        );
        when(cpuUsageRepository.findByTimestampBetween(startTime, endTime)).thenReturn(fakeData);

        // When
        List<CpuUsage> result = cpuMonitoringService.getCpuUsageByMinute(startTime, endTime).getCpuUsage();

        // Then
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("시 단위 조회")
    void testGetCpuUsageStatsByHour() {
        // Given
        LocalDate date = LocalDate.now();
        List<CpuUsage> fakeData = Arrays.asList(
                new CpuUsage(50, LocalDateTime.now().minusHours(2)),
                new CpuUsage(60, LocalDateTime.now().minusHours(1)),
                new CpuUsage(70, LocalDateTime.now())
        );
        when(cpuUsageRepository.findByTimestampBetween(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(fakeData);

        // When
        Map<LocalDateTime, CpuUsageStats> result = cpuMonitoringService.getCpuUsageStatsByHour(date, date).getCpuUsage();

        // Then
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("일 단위 조회")
    void testGetCpuUsageStatsByDay() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now();
        List<CpuUsage> fakeData = Arrays.asList(
                new CpuUsage(50, LocalDateTime.now().minusDays(1).plusHours(2)),
                new CpuUsage(60, LocalDateTime.now().minusDays(1).plusHours(3)),
                new CpuUsage(70, LocalDateTime.now().minusDays(1).plusHours(4))
        );
        when(cpuUsageRepository.findByTimestampBetween(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(fakeData);

        // When
        Map<LocalDate, CpuUsageStats> result = cpuMonitoringService.getCpuUsageStatsByDay(startDate, endDate).getCpuUsage();

        // Then
        assertEquals(1, result.size());
    }
}
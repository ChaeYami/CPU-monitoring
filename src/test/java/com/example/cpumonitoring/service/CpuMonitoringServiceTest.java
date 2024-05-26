package com.example.cpumonitoring.service;

import com.example.cpumonitoring.component.CpuUsageCollector;
import com.example.cpumonitoring.entity.CpuUsage;
import com.example.cpumonitoring.repository.CpuUsageRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class CpuMonitoringServiceTest {
    @Mock
    private CpuUsageRepository cpuUsageRepository;

    @Mock
    private CpuUsageCollector cpuUsageCollector;

    @InjectMocks
    private CpuUsageService cpuUsageService;
    private TestLogger logger = TestLoggerFactory.getTestLogger(CpuUsageService.class);

    @BeforeEach
    public void setUp() {
        // 각 테스트 시작 전에 로깅 이벤트 초기화
        TestLoggerFactory.clear();
    }

    @Test
    public void testSaveCpuUsage() {
        // Given
        Double mockCpuUsage = 7.928;
        when(cpuUsageCollector.collectCpuUsage()).thenReturn(mockCpuUsage);

        // When
        cpuUsageService.saveCpuUsage();

        // Then
        verify(cpuUsageCollector, times(1)).collectCpuUsage();
        verify(cpuUsageRepository, times(1)).save(any(CpuUsage.class));
    }

    @Test
    public void testSaveCpuUsage_WhenCpuUsageIsNull() {
        // Given
        when(cpuUsageCollector.collectCpuUsage()).thenReturn(null);

        // When
        cpuUsageService.saveCpuUsage();

        // Then
        verify(cpuUsageCollector, times(1)).collectCpuUsage();
        verify(cpuUsageRepository, never()).save(any(CpuUsage.class));
    }

    @Test
    public void testSaveCpuUsage_WhenExceptionThrown() {
        // Given
        when(cpuUsageCollector.collectCpuUsage()).thenThrow(new RuntimeException("Test exception"));

        // When
        cpuUsageService.saveCpuUsage();

        // Then
        verify(cpuUsageCollector, times(1)).collectCpuUsage();
        verify(cpuUsageRepository, never()).save(any(CpuUsage.class));

        // Verify logging
        List<LoggingEvent> loggingEvents = logger.getLoggingEvents();
        System.out.println(loggingEvents);
//        assertEquals(1, loggingEvents.size());
//        assertEquals("Failed to save CPU usage data : Test exception", loggingEvents.get(0).getMessage());
    }
}
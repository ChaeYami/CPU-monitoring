package com.example.cpumonitoring.service;

import com.example.cpumonitoring.component.CpuUsageCollector;
import com.example.cpumonitoring.entity.CpuUsage;
import com.example.cpumonitoring.repository.CpuUsageRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class CpuUsageServiceTest {
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
    @DisplayName("DB 저장")
    void testSaveCpuUsage() {
        // Given
        CpuUsage cpuUsage = new CpuUsage("7.93%", LocalDateTime.now());
        when(cpuUsageRepository.save(cpuUsage)).thenReturn(cpuUsage);

        // When
        CpuUsage savedCpuUsage = cpuUsageRepository.save(cpuUsage);

        // Then
        assertNotNull(savedCpuUsage);
        assertEquals("7.93%", savedCpuUsage.getCpuUsage());
        verify(cpuUsageRepository, times(1)).save(cpuUsage);
    }

    @Test
    @DisplayName("지정한 구간 DB 조회")
    void testFindByTimestampBetween() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        CpuUsage cpuUsage1 = new CpuUsage("7.93%", start.plusHours(1));
        CpuUsage cpuUsage2 = new CpuUsage("6.75%", start.plusHours(2));
        List<CpuUsage> expectedUsages = Arrays.asList(cpuUsage1, cpuUsage2);

        when(cpuUsageRepository.findByTimestampBetween(start, end)).thenReturn(expectedUsages);

        // When
        List<CpuUsage> foundUsages = cpuUsageRepository.findByTimestampBetween(start, end);

        // Then
        assertEquals(2, foundUsages.size());
        assertEquals("7.93%", foundUsages.get(0).getCpuUsage());
        assertEquals("6.75%", foundUsages.get(1).getCpuUsage());
        verify(cpuUsageRepository, times(1)).findByTimestampBetween(start, end);
    }


    @Test
    @DisplayName("cpu 사용률 수집 및 저장")
    void testSaveCpuUsage_success() {
        // Given
        Double mockCpuUsage = 7.928;
        when(cpuUsageCollector.collectCpuUsage()).thenReturn(mockCpuUsage);

        // When
        cpuUsageService.saveCpuUsage();

        // Then
        verify(cpuUsageCollector, times(1)).collectCpuUsage();
        verify(cpuUsageRepository, times(1)).save(any(CpuUsage.class));

    }
}
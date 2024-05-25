package com.example.cpumonitoring.service;

import com.example.cpumonitoring.repository.CpuUsageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

class CpuMonitoringServiceTest {

    @Mock
    private CpuUsageRepository cpuUsageRepository;

    @InjectMocks
    private CpuMonitoringService cpuMonitoringService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

}
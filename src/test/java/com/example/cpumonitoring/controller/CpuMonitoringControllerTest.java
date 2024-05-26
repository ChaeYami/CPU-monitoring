package com.example.cpumonitoring.controller;

import com.example.cpumonitoring.entity.CpuUsage;
import com.example.cpumonitoring.model.CpuUsageStats;
import com.example.cpumonitoring.service.CpuMonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest(CpuMonitoringController.class)
@AutoConfigureMockMvc
class CpuMonitoringControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CpuMonitoringService cpuMonitoringService;

    private final LocalDateTime startTime = LocalDateTime.now().minusHours(1);
    private final LocalDateTime endTime = LocalDateTime.now();
    private final LocalDate date = LocalDate.now();

    @BeforeEach
    void setUp() {
        // 시 단위 조회 데이터 설정
        Map<Integer, CpuUsageStats> cpuUsageStatsByHour = new HashMap<>();
        cpuUsageStatsByHour.put(1, new CpuUsageStats(10.0, 50.0, 30.0));

        // 일 단위 조회 데이터 설정
        Map<LocalDate, CpuUsageStats> cpuUsageStatsByDay = new HashMap<>();
        cpuUsageStatsByDay.put(date, new CpuUsageStats(20.0, 60.0, 40.0));

        Mockito.when(cpuMonitoringService.getCpuUsageByMinute(any(), any()))
                .thenReturn(Collections.singletonList(new CpuUsage("30.0%", LocalDateTime.now())));
        Mockito.when(cpuMonitoringService.getCpuUsageStatsByHour(any()))
                .thenReturn(cpuUsageStatsByHour);
        Mockito.when(cpuMonitoringService.getCpuUsageStatsByDay(any(), any()))
                .thenReturn(cpuUsageStatsByDay);
    }

    @Test
    @DisplayName("분 단위 조회")
    void testGetCpuUsageByMinute() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/cpumonitoring/minute")
                        .param("startTime", startTime.toString())
                        .param("endTime", endTime.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usage").value("30.0%"));
    }

    @Test
    @DisplayName("시 단위 조회")
    void testGetCpuUsageStatsByHour() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/cpumonitoring/hour")
                        .param("date", date.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['1'].minUsage").value(10.0))
                .andExpect(jsonPath("$['1'].maxUsage").value(50.0))
                .andExpect(jsonPath("$['1'].averageUsage").value(30.0));
    }

    @Test
    @DisplayName("일 단위 조회")
    void testGetCpuUsageStatsByDay() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/cpumonitoring/day")
                        .param("startDate", date.toString())
                        .param("endDate", date.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['" + date.toString() + "'].minUsage").value(20.0))
                .andExpect(jsonPath("$['" + date.toString() + "'].maxUsage").value(60.0))
                .andExpect(jsonPath("$['" + date.toString() + "'].averageUsage").value(40.0));
    }

}
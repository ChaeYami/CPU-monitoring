package com.example.cpumonitoring.controller;

import com.example.cpumonitoring.entity.CpuUsage;
import com.example.cpumonitoring.model.CpuUsageDateResponse;
import com.example.cpumonitoring.model.CpuUsageHourResponse;
import com.example.cpumonitoring.model.CpuUsageMinuteResponse;
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
import java.time.LocalTime;
import java.util.*;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
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

        // 분 단위 조회 데이터 설정
        List<CpuUsage> cpuUsagesByMinute = new ArrayList<>();
        cpuUsagesByMinute.add(new CpuUsage("30.0%", LocalDateTime.now().minusMinutes(30)));
        cpuUsagesByMinute.add(new CpuUsage("40.0%", LocalDateTime.now().minusMinutes(15)));
        Mockito.when(cpuMonitoringService.getCpuUsageByMinute(any(), any()))
                .thenReturn(new CpuUsageMinuteResponse(cpuUsagesByMinute, startTime, endTime));

        // 시 단위 조회 데이터 설정
        LocalDate date = LocalDate.now();
        List<CpuUsageStats> cpuUsageStatsList = new ArrayList<>();
        cpuUsageStatsList.add(new CpuUsageStats(10.0, 20.0, 15.0)); // 00:00부터 00:59까지 사용량
        cpuUsageStatsList.add(new CpuUsageStats(10.0, 30.0, 20.0)); // 01:00부터 01:59까지 사용량
        cpuUsageStatsList.add(new CpuUsageStats(10.0, 40.0, 25.0)); // 02:00부터 02:59까지 사용량

        Map<LocalDateTime, CpuUsageStats> cpuUsagesByHour = new LinkedHashMap<>();
        LocalDateTime currentHour = date.atStartOfDay();
        for (CpuUsageStats stats : cpuUsageStatsList) {
            cpuUsagesByHour.put(currentHour, stats);
            currentHour = currentHour.plusHours(1);
        }

        Mockito.when(cpuMonitoringService.getCpuUsageStatsByHour(any()))
                .thenReturn(new CpuUsageHourResponse(cpuUsagesByHour, date));

        Mockito.when(cpuMonitoringService.getCpuUsageStatsByHour(any()))
                .thenReturn(new CpuUsageHourResponse(cpuUsagesByHour, date));


        // 일 단위 조회 데이터 설정
        Map<LocalDate, CpuUsageStats> cpuUsageStatsByDay = new HashMap<>();
        cpuUsageStatsByDay.put(LocalDate.now(), new CpuUsageStats(20.0, 60.0, 40.0));


        LocalDate startDate = LocalDate.now().minusDays(2);
        LocalDate endDate = LocalDate.now();
        List<CpuUsage> cpuUsagesByDay = new ArrayList<>();
        cpuUsagesByDay.add(new CpuUsage("25.0%", LocalDateTime.now().minusDays(2)));
        cpuUsagesByDay.add(new CpuUsage("35.0%", LocalDateTime.now().minusDays(1)));
        cpuUsagesByDay.add(new CpuUsage("45.0%", LocalDateTime.now()));
        Mockito.when(cpuMonitoringService.getCpuUsageStatsByDay(any(), any()))
                .thenReturn(new CpuUsageDateResponse(cpuUsageStatsByDay, startDate, endDate));
    }

    @Test
    @DisplayName("분 단위 조회")
    void testGetCpuUsageByMinute() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/cpumonitoring/minute")
                        .param("startTime", LocalDateTime.now().minusHours(1).toString())
                        .param("endTime", LocalDateTime.now().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpuUsage", hasSize(2)))
                .andExpect(jsonPath("$.cpuUsage[0].usage").value("30.0%"))
                .andExpect(jsonPath("$.cpuUsage[1].usage").value("40.0%"));
    }

    @Test
    @DisplayName("시 단위 조회")
    void testGetCpuUsageStatsByHour() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/cpumonitoring/hour")
                        .param("date", date.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpuUsage", hasKey("2024-05-27T00:00")))
                .andExpect(jsonPath("$.cpuUsage['2024-05-27T00:00'].minUsage").value(10.0))
                .andExpect(jsonPath("$.cpuUsage['2024-05-27T00:00'].maxUsage").value(20.0))
                .andExpect(jsonPath("$.cpuUsage['2024-05-27T00:00'].averageUsage").value(15.0))
                .andExpect(jsonPath("$.cpuUsage", hasKey("2024-05-27T01:00")))
                .andExpect(jsonPath("$.cpuUsage['2024-05-27T01:00'].minUsage").value(10.0))
                .andExpect(jsonPath("$.cpuUsage['2024-05-27T01:00'].maxUsage").value(30.0))
                .andExpect(jsonPath("$.cpuUsage['2024-05-27T01:00'].averageUsage").value(20.0))
                .andExpect(jsonPath("$.cpuUsage", hasKey("2024-05-27T02:00")))
                .andExpect(jsonPath("$.cpuUsage['2024-05-27T02:00'].minUsage").value(10.0))
                .andExpect(jsonPath("$.cpuUsage['2024-05-27T02:00'].maxUsage").value(40.0))
                .andExpect(jsonPath("$.cpuUsage['2024-05-27T02:00'].averageUsage").value(25.0));
    }



    @Test
    @DisplayName("일 단위 조회")
    void testGetCpuUsageStatsByDay() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/cpumonitoring/day")
                        .param("startDate", LocalDate.now().minusDays(2).toString())
                        .param("endDate", LocalDate.now().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpuUsage").isMap())
                .andExpect(jsonPath("$.cpuUsage['" + LocalDate.now().toString() + "'].minUsage").value(20.0))
                .andExpect(jsonPath("$.cpuUsage['" + LocalDate.now().toString() + "'].maxUsage").value(60.0))
                .andExpect(jsonPath("$.cpuUsage['" + LocalDate.now().toString() + "'].averageUsage").value(40.0));
    }


}
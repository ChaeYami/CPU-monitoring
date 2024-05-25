package com.example.cpumonitoring.service;

import com.example.cpumonitoring.repository.CpuUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CpuMonitoringService {
    private final CpuUsageRepository cpuUsageRepository;

    // TODO : 지정한 시간 구간의 분 단위 CPU 사용률 조회

    // TODO : 지정한 날짜의 시 단위 CPU 최소/최대/평균 사용률 조회

    // TODO : 지정한 날짜 구간의 일 단위 CPU 최소/최대/평균 사용률 조회

}
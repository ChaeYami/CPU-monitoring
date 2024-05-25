package com.example.cpumonitoring.repository;

import com.example.cpumonitoring.entity.CpuUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CpuUsageRepository extends JpaRepository<CpuUsage, Long> {

    List<CpuUsage> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    // 분, 시, 일 단위

}
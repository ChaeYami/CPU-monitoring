package com.example.cpumonitoring.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "cpu_monitoring")
public class CpuUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    private double  cpuUsage;

    private LocalDateTime timestamp;

    public CpuUsage(double cpuUsage, LocalDateTime timestamp) {
        this.cpuUsage = cpuUsage;
        this.timestamp = timestamp;
    }
}
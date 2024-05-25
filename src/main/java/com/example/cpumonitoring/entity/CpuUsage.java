package com.example.cpumonitoring.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    private String usage;

    private LocalDateTime timestamp;

    public CpuUsage(String usage, LocalDateTime timestamp) {
        this.usage = usage;
        this.timestamp = timestamp;
    }
}
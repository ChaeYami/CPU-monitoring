package com.example.cpumonitoring.config;

import com.example.cpumonitoring.exception.CpuUsageCollectionException;
import com.example.cpumonitoring.exception.InvalidDateTimeRangeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid parameter: " + ex.getName());
    }

    @ExceptionHandler(InterruptedException.class)
    public ResponseEntity<String> handleInterruptedException(InterruptedException ex) {
        log.error("InterruptedException occurred: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
    }

    @ExceptionHandler(CpuUsageCollectionException.class)
    public ResponseEntity<String> handleCpuUsageCollectionException(CpuUsageCollectionException ex) {
        log.error("Failed to collect CPU usage data: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to collect CPU usage data");
    }

    @ExceptionHandler(InvalidDateTimeRangeException.class)
    public ResponseEntity<String> handleInvalidDateRangeException(InvalidDateTimeRangeException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
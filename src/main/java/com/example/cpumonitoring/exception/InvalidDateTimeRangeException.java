package com.example.cpumonitoring.exception;

public class InvalidDateTimeRangeException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "startDate(startTime) cannot be after endDate(endTime)";

    public InvalidDateTimeRangeException() {
        super(DEFAULT_MESSAGE);
    }

    public InvalidDateTimeRangeException(String message) {
        super(message);
    }
}

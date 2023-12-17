package ru.practicum.ewm.exception;

public class StatsServiceException extends RuntimeException {
    public StatsServiceException(String message) {
        super(message);
    }
}
package ru.practicum.ewm.exception;

public class OperationConditionsFailureException extends RuntimeException {
    public OperationConditionsFailureException(String message) {
        super(message);
    }
}
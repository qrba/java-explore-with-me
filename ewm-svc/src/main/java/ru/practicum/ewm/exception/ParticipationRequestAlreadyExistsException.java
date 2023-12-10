package ru.practicum.ewm.exception;

public class ParticipationRequestAlreadyExistsException extends RuntimeException {
    public ParticipationRequestAlreadyExistsException(String message) {
        super(message);
    }
}
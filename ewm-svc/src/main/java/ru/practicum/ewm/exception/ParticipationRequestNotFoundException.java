package ru.practicum.ewm.exception;

public class ParticipationRequestNotFoundException extends RuntimeException {
    public ParticipationRequestNotFoundException(String message) {
        super(message);
    }
}
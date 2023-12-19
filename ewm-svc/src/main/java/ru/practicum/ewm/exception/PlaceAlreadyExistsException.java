package ru.practicum.ewm.exception;

public class PlaceAlreadyExistsException extends RuntimeException {
    public PlaceAlreadyExistsException(String message) {
        super(message);
    }
}
package ru.practicum.ewm.exception;

public class CompilationAlreadyExistsException extends RuntimeException {
    public CompilationAlreadyExistsException(String message) {
        super(message);
    }
}
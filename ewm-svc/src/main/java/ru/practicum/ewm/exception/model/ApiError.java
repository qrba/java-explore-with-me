package ru.practicum.ewm.exception.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
public class ApiError {
    private final StackTraceElement[] errors;
    private final String message;
    private final String reason;
    private final HttpStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;
}

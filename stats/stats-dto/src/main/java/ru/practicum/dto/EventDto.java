package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
public class EventDto {
    @NotBlank(message = "Идентификатор сервиса не может быть пустым")
    private final String app;
    @NotBlank(message = "URI не может быть пустым")
    private final String uri;
    @NotBlank(message = "IP-адрес не может быть пустым")
    private final String ip;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @PastOrPresent(message = "Дата и время запроса не могут быть в будущем")
    private final LocalDateTime timestamp;
}
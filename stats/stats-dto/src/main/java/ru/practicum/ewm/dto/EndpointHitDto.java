package ru.practicum.ewm.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@RequiredArgsConstructor
public class EndpointHitDto {
    @NotBlank(message = "Идентификатор сервиса не может быть пустым")
    private final String app;
    @NotBlank(message = "URI не может быть пустым")
    private final String uri;
    @NotBlank(message = "IP-адрес не может быть пустым")
    private final String ip;
    @NotBlank(message = "Время обращения не может быть пустым")
    private final String timestamp;
}
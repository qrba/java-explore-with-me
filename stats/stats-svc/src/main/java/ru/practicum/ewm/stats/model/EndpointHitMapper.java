package ru.practicum.ewm.stats.model;

import ru.practicum.ewm.dto.EndpointHitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EndpointHitMapper {
    public static EndpointHit endpointHitFromDto(EndpointHitDto endpointHitDto) {
        return new EndpointHit(
                null,
                endpointHitDto.getApp(),
                endpointHitDto.getUri(),
                endpointHitDto.getIp(),
                LocalDateTime.parse(endpointHitDto.getTimestamp(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }
}

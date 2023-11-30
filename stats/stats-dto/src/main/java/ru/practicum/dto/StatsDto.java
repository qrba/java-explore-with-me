package ru.practicum.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class StatsDto {
    private final String app;
    private final String uri;
    private final Long hits;
}
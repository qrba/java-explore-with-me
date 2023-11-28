package ru.practicum.statsvc.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class EventDtoOut {
    private final String app;
    private final String uri;
    private final Long hits;
}
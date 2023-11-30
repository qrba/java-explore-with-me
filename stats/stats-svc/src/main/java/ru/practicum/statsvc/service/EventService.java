package ru.practicum.statsvc.service;

import ru.practicum.dto.EventDto;
import ru.practicum.dto.StatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    void saveEvent(EventDto eventDto);

    List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}

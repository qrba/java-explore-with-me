package ru.practicum.statsvc.service;

import ru.practicum.statsvc.model.EventDtoIn;
import ru.practicum.statsvc.model.EventDtoOut;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    void saveEvent(EventDtoIn eventDtoIn);

    List<EventDtoOut> getEvents(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}

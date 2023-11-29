package ru.practicum.statsvc.model;

import ru.practicum.dto.EventDto;

public class EventMapper {
    public static Event eventFromDto(EventDto eventDto) {
        return new Event(
                null,
                eventDto.getApp(),
                eventDto.getUri(),
                eventDto.getIp(),
                eventDto.getTimestamp()
        );
    }
}

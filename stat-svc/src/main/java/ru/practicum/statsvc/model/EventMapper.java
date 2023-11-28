package ru.practicum.statsvc.model;

public class EventMapper {
    public static Event eventFromDto(EventDtoIn eventDtoIn) {
        return new Event(
                null,
                eventDtoIn.getApp(),
                eventDtoIn.getUri(),
                eventDtoIn.getIp(),
                eventDtoIn.getTimestamp()
        );
    }
}

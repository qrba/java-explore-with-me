package ru.practicum.ewm.event.model.dto;

import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;

import static ru.practicum.ewm.category.model.dto.CategoryMapper.categoryToCategoryDto;
import static ru.practicum.ewm.user.model.dto.UserMapper.userToShortDto;

public class EventMapper {
    public static EventShortDto eventToShortDto(Event event, int confirmedRequests, long hits) {
        return new EventShortDto(
                event.getId(),
                userToShortDto(event.getInitiator()),
                event.getAnnotation(),
                categoryToCategoryDto(event.getCategory()),
                event.getEventDate(),
                event.getPaid(),
                event.getTitle(),
                confirmedRequests,
                hits
        );
    }

    public static EventFullDto eventToFullDto(Event event, int confirmedRequests, long hits) {
        return new EventFullDto(
                event.getId(),
                userToShortDto(event.getInitiator()),
                event.getAnnotation(),
                categoryToCategoryDto(event.getCategory()),
                event.getDescription(),
                event.getEventDate(),
                new Location(event.getLat(), event.getLon()),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getRequestModeration(),
                event.getTitle(),
                event.getCreatedOn(),
                event.getState(),
                event.getPublishedOn(),
                confirmedRequests,
                hits
        );
    }

    public static Event eventFromNewEventDto(NewEventDto newEventDto, User initiator, Category category) {
        return new Event(
                null,
                initiator,
                newEventDto.getAnnotation(),
                category,
                newEventDto.getDescription(),
                newEventDto.getEventDate(),
                newEventDto.getLocation().getLat(),
                newEventDto.getLocation().getLon(),
                newEventDto.getPaid(),
                newEventDto.getParticipantLimit(),
                newEventDto.getRequestModeration(),
                newEventDto.getTitle(),
                LocalDateTime.now(),
                EventState.PENDING,
                null,
                null
        );
    }
}
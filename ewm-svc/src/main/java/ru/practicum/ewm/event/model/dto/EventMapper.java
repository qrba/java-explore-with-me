package ru.practicum.ewm.event.model.dto;

import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.event.model.Coordinate;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;

import static ru.practicum.ewm.category.model.dto.CategoryMapper.categoryToCategoryDto;
import static ru.practicum.ewm.user.model.dto.UserMapper.userToShortDto;

public class EventMapper {
    public static EventShortDto eventToShortDto(Event event, Integer confirmedRequests, Long hits) {
        return new EventShortDto(
                event.getId(),
                userToShortDto(event.getInitiator()),
                event.getAnnotation(),
                categoryToCategoryDto(event.getCategory()),
                event.getEventDate(),
                event.isPaid(),
                event.getTitle(),
                confirmedRequests,
                hits
        );
    }

    public static EventFullDto eventToFullDto(Event event, Integer confirmedRequests, Long hits) {
        return new EventFullDto(
                event.getId(),
                userToShortDto(event.getInitiator()),
                event.getAnnotation(),
                categoryToCategoryDto(event.getCategory()),
                event.getDescription(),
                event.getEventDate(),
                new Coordinate(event.getLat(), event.getLon()),
                event.isPaid(),
                event.getParticipantLimit(),
                event.isRequestModeration(),
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
                newEventDto.getCoordinate().getLat(),
                newEventDto.getCoordinate().getLon(),
                newEventDto.isPaid(),
                newEventDto.getParticipantLimit(),
                newEventDto.getRequestModeration() == null || newEventDto.getRequestModeration(),
                newEventDto.getTitle(),
                LocalDateTime.now(),
                EventState.PENDING,
                null
        );
    }
}
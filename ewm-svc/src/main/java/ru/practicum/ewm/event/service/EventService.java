package ru.practicum.ewm.event.service;

import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.SortType;
import ru.practicum.ewm.event.model.dto.EventFullDto;
import ru.practicum.ewm.event.model.dto.EventShortDto;
import ru.practicum.ewm.event.model.dto.NewEventDto;
import ru.practicum.ewm.event.model.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.model.dto.UpdateEventUserRequest;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventFullDto getEventById(Integer id, HttpServletRequest request);

    List<EventShortDto> getEvents(String text,
                                  List<Integer> categories,
                                  Boolean paid,
                                  LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd,
                                  Boolean onlyAvailable,
                                  SortType sort,
                                  Integer from,
                                  Integer size,
                                  HttpServletRequest request
    );

    EventFullDto addEvent(Integer userId, NewEventDto newEventDto);

    EventFullDto updateEventByInitiator(Integer initiatorId, Integer eventId, UpdateEventUserRequest updateEventUserRequest);

    List<EventShortDto> getEventsByInitiatorId(Integer initiatorId, Integer from, Integer size);

    EventFullDto getEventByIdAndInitiatorId(Integer initiatorId, Integer eventId);

    List<EventFullDto> getEventsByAdmin(
            List<Integer> users,
            List<EventState> states,
            List<Integer> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Integer from,
            Integer size
    );

    EventFullDto updateEventByAdmin(Integer eventId, UpdateEventAdminRequest updateRequest);

    List<EventShortDto> getEventsInLocation(Integer locationId, Integer from, Integer size);
}
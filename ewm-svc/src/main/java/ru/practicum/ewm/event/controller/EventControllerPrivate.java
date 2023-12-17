package ru.practicum.ewm.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.model.dto.EventFullDto;
import ru.practicum.ewm.event.model.dto.EventShortDto;
import ru.practicum.ewm.event.model.dto.NewEventDto;
import ru.practicum.ewm.event.model.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.participationrequest.model.dto.ParticipationRequestDto;
import ru.practicum.ewm.participationrequest.model.dto.ParticipationRequestStatusUpdateRequest;
import ru.practicum.ewm.participationrequest.model.dto.ParticipationRequestStatusUpdateResult;
import ru.practicum.ewm.participationrequest.service.ParticipationRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class EventControllerPrivate {
    private final EventService eventService;
    private final ParticipationRequestService participationRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(
            @PathVariable Integer userId,
            @Valid @RequestBody NewEventDto newEventDto
    ) {
        return eventService.addEvent(userId, newEventDto);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(
            @PathVariable Integer userId,
            @PathVariable Integer eventId,
            @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest
    ) {
        return eventService.updateEventByInitiator(userId, eventId, updateEventUserRequest);
    }

    @GetMapping
    public List<EventShortDto> getEventsByUserId(
            @PathVariable Integer userId,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size
    ) {
        return eventService.getEventsByInitiatorId(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventByIdAndUserId(
            @PathVariable Integer userId,
            @PathVariable Integer eventId
    ) {
        return eventService.getEventByIdAndInitiatorId(userId, eventId);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getParticipationRequests(
            @PathVariable Integer userId,
            @PathVariable Integer eventId
    ) {
        return participationRequestService.getParticipationRequestsByEventInitiator(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public ParticipationRequestStatusUpdateResult updateParticipationRequests(
            @PathVariable Integer userId,
            @PathVariable Integer eventId,
            @RequestBody ParticipationRequestStatusUpdateRequest updateRequest
    ) {
        return participationRequestService.updateParticipationRequests(userId, eventId, updateRequest);
    }
}
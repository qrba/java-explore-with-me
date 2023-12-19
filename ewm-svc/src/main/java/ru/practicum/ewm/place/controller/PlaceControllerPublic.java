package ru.practicum.ewm.place.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.model.dto.EventShortDto;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.place.model.dto.PlaceFullDto;
import ru.practicum.ewm.place.model.dto.PlaceShortDto;
import ru.practicum.ewm.place.service.PlaceService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/places")
@RequiredArgsConstructor
public class PlaceControllerPublic {
    private final PlaceService placeService;
    private final EventService eventService;

    @GetMapping("/{placeId}")
    public PlaceFullDto getPlaceById(@PathVariable int placeId) {
        return placeService.getPlaceById(placeId);
    }

    @GetMapping
    public List<PlaceShortDto> getPlaces(
            @RequestParam(required = false) String text,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size
    ) {
        return placeService.getPlaces(text, from, size);
    }

    @GetMapping("/{placeId}/events")
    public List<EventShortDto> getEventsInPlace(
            @PathVariable int placeId,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size
    ) {
        return eventService.getEventsInPlace(placeId, from, size);
    }
}
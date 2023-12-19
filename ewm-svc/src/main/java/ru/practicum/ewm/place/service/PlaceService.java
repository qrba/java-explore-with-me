package ru.practicum.ewm.place.service;

import ru.practicum.ewm.place.model.dto.NewPlaceDto;
import ru.practicum.ewm.place.model.dto.PlaceFullDto;
import ru.practicum.ewm.place.model.dto.PlaceShortDto;
import ru.practicum.ewm.place.model.dto.PlaceUpdateRequest;

import java.util.List;

public interface PlaceService {
    PlaceFullDto addPlace(NewPlaceDto newPlaceDto);

    void deletePlace(int placeId);

    PlaceFullDto updatePlace(PlaceUpdateRequest request);

    PlaceFullDto getPlaceById(int placeId);

    List<PlaceShortDto> getPlaces(String text, int from, int size);
}
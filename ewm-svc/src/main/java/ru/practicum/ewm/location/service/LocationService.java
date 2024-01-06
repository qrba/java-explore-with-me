package ru.practicum.ewm.location.service;

import ru.practicum.ewm.location.model.dto.NewLocationDto;
import ru.practicum.ewm.location.model.dto.LocationFullDto;
import ru.practicum.ewm.location.model.dto.LocationShortDto;
import ru.practicum.ewm.location.model.dto.LocationUpdateRequest;

import java.util.List;

public interface LocationService {
    LocationFullDto addLocation(NewLocationDto newLocationDto);

    void deleteLocation(int locationId);

    LocationFullDto updateLocation(LocationUpdateRequest request);

    LocationFullDto getLocationById(int locationId);

    List<LocationShortDto> getLocations(String text, int from, int size);
}
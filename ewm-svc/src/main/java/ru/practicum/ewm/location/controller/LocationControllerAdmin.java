package ru.practicum.ewm.location.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.location.model.dto.NewLocationDto;
import ru.practicum.ewm.location.model.dto.LocationFullDto;
import ru.practicum.ewm.location.model.dto.LocationUpdateRequest;
import ru.practicum.ewm.location.service.LocationService;

import javax.validation.Valid;

@RestController
@RequestMapping("/admin/locations")
@RequiredArgsConstructor
public class LocationControllerAdmin {
    private final LocationService locationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocationFullDto addLocation(@Valid @RequestBody NewLocationDto newLocationDto) {
        return locationService.addLocation(newLocationDto);
    }

    @DeleteMapping("/{locationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLocation(@PathVariable Integer locationId)  {
        locationService.deleteLocation(locationId);
    }

    @PatchMapping("/{locationId}")
    public LocationFullDto updateLocation(
            @PathVariable Integer locationId,
            @Valid @RequestBody LocationUpdateRequest request
    ) {
        request.setId(locationId);
        return locationService.updateLocation(request);
    }
}
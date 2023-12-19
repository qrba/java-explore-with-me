package ru.practicum.ewm.place.controller;

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
import ru.practicum.ewm.place.model.dto.NewPlaceDto;
import ru.practicum.ewm.place.model.dto.PlaceFullDto;
import ru.practicum.ewm.place.model.dto.PlaceUpdateRequest;
import ru.practicum.ewm.place.service.PlaceService;

import javax.validation.Valid;

@RestController
@RequestMapping("/admin/places")
@RequiredArgsConstructor
public class PlaceControllerAdmin {
    private final PlaceService placeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlaceFullDto addPlace(@Valid @RequestBody NewPlaceDto newPlaceDto) {
        return placeService.addPlace(newPlaceDto);
    }

    @DeleteMapping("/{placeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePlace(@PathVariable Integer placeId)  {
        placeService.deletePlace(placeId);
    }

    @PatchMapping("/{placeId}")
    public PlaceFullDto updatePlace(@PathVariable Integer placeId, @Valid @RequestBody PlaceUpdateRequest request) {
        request.setId(placeId);
        return placeService.updatePlace(request);
    }
}
package ru.practicum.ewm.place.model.dto;

import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.place.model.Place;

public class PlaceMapper {
    public static Place placeFromNewPlaceDto(NewPlaceDto newPlaceDto) {
        return new Place(
                null,
                newPlaceDto.getName(),
                newPlaceDto.getDescription(),
                newPlaceDto.getLocation().getLat(),
                newPlaceDto.getLocation().getLon(),
                newPlaceDto.getRadius()
        );
    }

    public static PlaceFullDto placeToFullDto(Place place) {
        return new PlaceFullDto(
                place.getId(),
                place.getName(),
                place.getDescription(),
                new Location(place.getLat(), place.getLon()),
                place.getRadius()
        );
    }

    public static PlaceShortDto placeToShortDto(Place place) {
        return new PlaceShortDto(
                place.getId(),
                place.getName()
        );
    }
}
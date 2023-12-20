package ru.practicum.ewm.location.model.dto;

import ru.practicum.ewm.event.model.Coordinate;
import ru.practicum.ewm.location.model.Location;

public class LocationMapper {
    public static Location locationFromNewLocationDto(NewLocationDto newLocationDto) {
        return new Location(
                null,
                newLocationDto.getName(),
                newLocationDto.getDescription(),
                newLocationDto.getCoordinate().getLat(),
                newLocationDto.getCoordinate().getLon(),
                newLocationDto.getRadius()
        );
    }

    public static LocationFullDto locationToFullDto(Location location) {
        return new LocationFullDto(
                location.getId(),
                location.getName(),
                location.getDescription(),
                new Coordinate(location.getLat(), location.getLon()),
                location.getRadius()
        );
    }

    public static LocationShortDto locationToShortDto(Location location) {
        return new LocationShortDto(
                location.getId(),
                location.getName()
        );
    }
}
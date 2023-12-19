package ru.practicum.ewm.place.model.dto;

import lombok.Data;
import ru.practicum.ewm.event.model.Location;

@Data
public class PlaceFullDto {
    private final int id;
    private final String name;
    private final String description;
    private final Location location;
    private final double radius;
}
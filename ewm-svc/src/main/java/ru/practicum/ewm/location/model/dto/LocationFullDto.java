package ru.practicum.ewm.location.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.practicum.ewm.event.model.Coordinate;

@Data
public class LocationFullDto {
    private final int id;
    private final String name;
    private final String description;
    @JsonProperty("location")
    private final Coordinate coordinate;
    private final double radius;
}
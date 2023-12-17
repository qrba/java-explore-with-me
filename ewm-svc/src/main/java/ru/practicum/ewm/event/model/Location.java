package ru.practicum.ewm.event.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Location {
    private final double lat;
    private final double lon;
}
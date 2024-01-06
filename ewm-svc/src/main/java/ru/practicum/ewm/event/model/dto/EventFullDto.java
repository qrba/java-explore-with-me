package ru.practicum.ewm.event.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.event.model.Coordinate;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.user.model.dto.UserShortDto;
import ru.practicum.ewm.category.model.dto.CategoryDto;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
public class EventFullDto {
    private final Integer id;
    private final UserShortDto initiator;
    private final String annotation;
    private final CategoryDto category;
    private final String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime eventDate;
    @JsonProperty("location")
    private final Coordinate coordinate;
    private final Boolean paid;
    private final Integer participantLimit;
    private final Boolean requestModeration;
    private final String title;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createdOn;
    private final EventState state;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime publishedOn;
    private final Integer confirmedRequests;
    private final Long views;
}
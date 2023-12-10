package ru.practicum.ewm.event.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.user.model.dto.UserShortDto;
import ru.practicum.ewm.category.model.dto.CategoryDto;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
public class EventShortDto {
    private final Integer id;
    private final UserShortDto initiator;
    private final String annotation;
    private final CategoryDto category;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime eventDate;
    private final Boolean paid;
    private final String title;
    private final Integer confirmedRequests;
    private final Long views;
}
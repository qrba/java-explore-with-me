package ru.practicum.ewm.event.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.model.ActionState;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
public class UpdateEventUserRequest {
    @Size(min = 20, max = 2000, message = "Размер аннотации должен быть не менее 20 и не более 2000 символов")
    private final String annotation;
    private final Integer category;
    @Size(min = 20, max = 7000, message = "Размер описания должен быть не менее 20 и не более 7000 символов")
    private final String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime eventDate;
    private final Location location;
    private final Boolean paid;
    private final Integer participantLimit;
    private final Boolean requestModeration;
    @Size(min = 3, max = 120, message = "Размер заголовка должен быть не менее 3 и не более 120 символов")
    private final String title;
    private final ActionState stateAction;
}
package ru.practicum.ewm.event.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.ewm.event.model.Location;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class NewEventDto {
    @NotBlank(message = "Для события должна быть заполнена аннотация")
    @Size(min = 20, max = 2000, message = "Размер аннотации должен быть не менее 20 и не более 2000 символов")
    private final String annotation;
    @NotNull(message = "Для события должна быть указана категория")
    private final Integer category;
    @NotBlank(message = "Для события должно быть заполнено описание")
    @Size(min = 20, max = 7000)
    private final String description;
    @NotNull(message = "Для события должны быть указаны дата и время начала")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime eventDate;
    @NotNull(message = "Для события должна быть указана локация проведения")
    private final Location location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    @NotBlank(message = "Для события должен быть заполнен заголовок")
    @Size(min = 3, max = 120, message = "Размер заголовка должен быть не менее 3 и не более 120 символов")
    private final String title;
}
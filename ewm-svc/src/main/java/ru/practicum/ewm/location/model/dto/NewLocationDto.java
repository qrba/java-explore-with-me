package ru.practicum.ewm.location.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.practicum.ewm.event.model.Coordinate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@Data
public class NewLocationDto {
    @NotBlank(message = "Название локации не может быть пустым")
    @Size(min = 1, max = 50, message = "Размер названия локации должен быть между 1 и 50 символами")
    private final String name;
    @NotBlank(message = "Для локации должно быть заполнено описание")
    @Size(min = 20, max = 2000, message = "Размер описания должен быть не менее 20 и не более 2000 символов")
    private final String description;
    @JsonProperty("location")
    @NotNull(message = "Для локации должны быть указаны координаты")
    private final Coordinate coordinate;
    @NotNull(message = "Для локации должен быть указан радиус")
    @Positive
    private final double radius;
}
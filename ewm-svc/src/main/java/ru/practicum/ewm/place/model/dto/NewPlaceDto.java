package ru.practicum.ewm.place.model.dto;

import lombok.Data;
import ru.practicum.ewm.event.model.Location;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@Data
public class NewPlaceDto {
    @NotBlank(message = "Название места не может быть пустым")
    @Size(min = 1, max = 50, message = "Размер названия места должен быть между 1 и 50 символами")
    private final String name;
    @NotBlank(message = "Для места должно быть заполнено описание")
    @Size(min = 20, max = 2000, message = "Размер описания должен быть не менее 20 и не более 2000 символов")
    private final String description;
    @NotNull(message = "Для места должны быть указаны координаты")
    private final Location location;
    @NotNull(message = "Для места должен быть указан радиус")
    @Positive
    private final double radius;
}
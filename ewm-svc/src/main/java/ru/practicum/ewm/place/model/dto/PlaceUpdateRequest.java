package ru.practicum.ewm.place.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.model.Location;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceUpdateRequest {
    private Integer id;
    @Size(min = 2, max = 250, message = "Размер названия места должен быть между 1 и 50 символами")
    private String name;
    @Size(min = 20, max = 2000, message = "Размер описания места должен быть не менее 20 и не более 2000 символов")
    private String description;
    private Location location;
    @Positive
    private Double radius;
}
package ru.practicum.ewm.location.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.model.Coordinate;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationUpdateRequest {
    private Integer id;
    @Size(min = 2, max = 250, message = "Размер названия локации должен быть между 1 и 50 символами")
    private String name;
    @Size(min = 20, max = 2000, message = "Размер описания локации должен быть не менее 20 и не более 2000 символов")
    private String description;
    @JsonProperty("location")
    private Coordinate coordinate;
    @Positive
    private Double radius;
}
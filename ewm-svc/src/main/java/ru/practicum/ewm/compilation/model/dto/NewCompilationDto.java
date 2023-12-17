package ru.practicum.ewm.compilation.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@AllArgsConstructor
public class NewCompilationDto {
    private Boolean pinned;
    @NotBlank(message = "Для подборки должен быть указан заголовок")
    @Size(min = 1, max = 50, message = "Размер заголовка подборки должен быть не менее 1 и не более 50 символов")
    private final String title;
    private final List<Integer> events;
}
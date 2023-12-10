package ru.practicum.ewm.compilation.model.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.Size;
import java.util.List;

@Data
@RequiredArgsConstructor
public class UpdateCompilationRequest {
    private Boolean pinned;
    @Size(min = 1, max = 50, message = "Размер заголовка подборки должен быть не менее 1 и не более 50 символов")
    private final String title;
    private final List<Integer> events;
}
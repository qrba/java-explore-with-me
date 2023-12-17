package ru.practicum.ewm.category.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
public class CategoryDto {
    private Integer id;
    @NotBlank(message = "Название категории не может быть пустым")
    @Size(min = 1, max = 50)
    private final String name;
}
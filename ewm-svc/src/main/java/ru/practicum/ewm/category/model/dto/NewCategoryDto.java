package ru.practicum.ewm.category.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class NewCategoryDto {
    @NotBlank(message = "Название категории не может быть пустым")
    @Size(min = 1, max = 50)
    private String name;
}
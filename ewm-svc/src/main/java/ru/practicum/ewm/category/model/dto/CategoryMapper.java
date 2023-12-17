package ru.practicum.ewm.category.model.dto;

import ru.practicum.ewm.category.model.Category;

public class CategoryMapper {
    public static Category categoryFromNewCategoryDto(NewCategoryDto newCategoryDto) {
        return new Category(
                null,
                newCategoryDto.getName()
        );
    }

    public static Category categoryFromCategoryDto(CategoryDto categoryDto) {
        return new Category(
                categoryDto.getId(),
                categoryDto.getName()
        );
    }

    public static CategoryDto categoryToCategoryDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName()
        );
    }
}
package ru.practicum.ewm.api.category;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.category.controller.CategoryControllerPublic;
import ru.practicum.ewm.category.model.dto.CategoryDto;
import ru.practicum.ewm.category.service.CategoryService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CategoryControllerPublic.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CategoryControllerPublicTest {
    @MockBean
    private CategoryService categoryService;
    private final MockMvc mvc;

    private final CategoryDto categoryDto = new CategoryDto(1, "name");

    @Test
    public void shouldGetCategoryById() throws Exception {
        Mockito
                .when(categoryService.getCategoryById(anyInt()))
                .thenReturn(categoryDto);

        mvc.perform(get("/categories/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryDto.getId()))
                .andExpect(jsonPath("$.name").value(categoryDto.getName()));
    }

    @Test
    public void shouldGetCategories() throws Exception {
        Mockito
                .when(categoryService.getCategories(anyInt(), anyInt()))
                .thenReturn(List.of(categoryDto));

        mvc.perform(get("/categories")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$.[0].id").value(categoryDto.getId()))
                .andExpect(jsonPath("$.[0].name").value(categoryDto.getName()));
    }
}
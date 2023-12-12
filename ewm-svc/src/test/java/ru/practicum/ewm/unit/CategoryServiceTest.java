package ru.practicum.ewm.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.model.dto.CategoryDto;
import ru.practicum.ewm.category.model.dto.NewCategoryDto;
import ru.practicum.ewm.category.service.CategoryServiceImpl;
import ru.practicum.ewm.category.storage.CategoryStorage;
import ru.practicum.ewm.exception.CategoryAlreadyExistsException;
import ru.practicum.ewm.exception.CategoryNotFoundException;
import ru.practicum.ewm.exception.OperationConditionsFailureException;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static ru.practicum.ewm.category.model.dto.CategoryMapper.categoryFromNewCategoryDto;
import static ru.practicum.ewm.category.model.dto.CategoryMapper.categoryToCategoryDto;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @Mock
    private CategoryStorage categoryStorage;
    @InjectMocks
    private CategoryServiceImpl categoryService;

    private final NewCategoryDto newCategoryDto = new NewCategoryDto("name");
    private final Category category = new Category(1, "name");

    @Test
    public void shouldAddCategory() {
        Mockito
                .when(categoryStorage.save(any(Category.class)))
                .then(returnsFirstArg());

        CategoryDto categoryDto = categoryToCategoryDto(
                categoryStorage.save(
                        categoryFromNewCategoryDto(
                                newCategoryDto
                        )
                )
        );

        assertThat(newCategoryDto.getName(), equalTo(categoryDto.getName()));
    }

    @Test
    public void shouldNotAddCategoryWhenNameNotUnique() {
        Mockito
                .when(categoryStorage.save(any(Category.class)))
                .thenThrow(DataIntegrityViolationException.class);

        CategoryAlreadyExistsException e = Assertions.assertThrows(
                CategoryAlreadyExistsException.class,
                () -> categoryService.addCategory(newCategoryDto)
        );

        assertThat(e.getMessage(), equalTo("Категория с названием 'name' уже существует"));
    }

    @Test
    public void shouldDeleteCategory() {
        Mockito
                .when(categoryStorage.existsById(anyInt()))
                .thenReturn(true);
        categoryService.deleteCategory(1);
        Mockito.verify(categoryStorage).deleteById(anyInt());
    }

    @Test
    public void shouldNotDeleteCategoryWhenCategoryNotFound() {
        CategoryNotFoundException e = Assertions.assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.deleteCategory(1)
        );

        assertThat(e.getMessage(), equalTo("Категория с id=1 не найдена"));
    }

    @Test
    public void shouldNotDeleteCategoryWhenCategoryNotEmpty() {
        Mockito
                .when(categoryStorage.existsById(anyInt()))
                .thenReturn(true);
        Mockito
                .doThrow(DataIntegrityViolationException.class)
                .when(categoryStorage).deleteById(anyInt());

        OperationConditionsFailureException e = Assertions.assertThrows(
                OperationConditionsFailureException.class,
                () -> categoryService.deleteCategory(1)
        );

        assertThat(e.getMessage(), equalTo("Категория с id=1 не пуста"));
    }

    @Test
    public void shouldGetCategories() {
        Mockito
                .when(categoryStorage.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(category)));
        List<CategoryDto> categoryDtoList = categoryService.getCategories(0, 10);

        assertThat(categoryDtoList.size(), equalTo(1));

        CategoryDto categoryDto = categoryDtoList.get(0);

        assertThat(category.getId(), equalTo(categoryDto.getId()));
        assertThat(category.getName(), equalTo(categoryDto.getName()));
    }

    @Test
    public void shouldGetCategoryById() {
        Mockito
                .when(categoryStorage.findById(anyInt()))
                .thenReturn(Optional.of(category));

        CategoryDto categoryDto = categoryService.getCategoryById(1);

        assertThat(category.getId(), equalTo(categoryDto.getId()));
        assertThat(category.getName(), equalTo(categoryDto.getName()));
    }

    @Test
    public void shouldNotGetCategoryByIdWhenCategoryNotFound() {
        Mockito
                .when(categoryStorage.findById(anyInt()))
                .thenReturn(Optional.empty());

        CategoryNotFoundException e = Assertions.assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.getCategoryById(1)
        );

        assertThat(e.getMessage(), equalTo("Категория с id=1 не найдена"));
    }

    @Test
    public void shouldUpdateCategory() {
        CategoryDto categoryDto = categoryToCategoryDto(category);
        Mockito
                .when(categoryStorage.findById(anyInt()))
                .thenReturn(Optional.of(category));
        CategoryDto updatedCategoryDto = categoryService.updateCategory(categoryDto);

        assertThat(categoryDto.getId(), equalTo(updatedCategoryDto.getId()));
        assertThat(categoryDto.getName(), equalTo(updatedCategoryDto.getName()));
    }

    @Test
    public void shouldNotUpdateCategoryWhenCategoryNotFound() {
        Mockito
                .when(categoryStorage.findById(anyInt()))
                .thenReturn(Optional.empty());

        CategoryNotFoundException e = Assertions.assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.updateCategory(categoryToCategoryDto(category))
        );

        assertThat(e.getMessage(), equalTo("Категория с id=1 не найдена"));
    }

    @Test
    public void shouldNotUpdateCategoryWhenNameNotUnique() {
        Mockito
                .when(categoryStorage.findById(anyInt()))
                .thenReturn(Optional.of(new Category(1, "other name")));
        Mockito
                .when(categoryStorage.existsByName(anyString()))
                .thenReturn(true);

        CategoryAlreadyExistsException e = Assertions.assertThrows(
                CategoryAlreadyExistsException.class,
                () -> categoryService.updateCategory(categoryToCategoryDto(category))
        );

        assertThat(e.getMessage(), equalTo("Категория с названием 'name' уже существует"));
    }
}
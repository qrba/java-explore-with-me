package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.model.dto.CategoryDto;
import ru.practicum.ewm.category.model.dto.CategoryMapper;
import ru.practicum.ewm.category.model.dto.NewCategoryDto;
import ru.practicum.ewm.category.storage.CategoryStorage;
import ru.practicum.ewm.exception.CategoryAlreadyExistsException;
import ru.practicum.ewm.exception.CategoryNotFoundException;
import ru.practicum.ewm.exception.OperationConditionsFailureException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.ewm.category.model.dto.CategoryMapper.categoryToCategoryDto;

@RequiredArgsConstructor
@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryStorage categoryStorage;

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Integer catId) {
        Optional<Category> categoryOptional = categoryStorage.findById(catId);
        if (categoryOptional.isEmpty()) throw new CategoryNotFoundException("Категория с id=" + catId + " не найдена");
        Category category = categoryOptional.get();
        log.info("Запрошена категория {}", category);
        return categoryToCategoryDto(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        log.info("Запрошен список категорий");
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        return categoryStorage.findAll(pageable).stream()
                .map(CategoryMapper::categoryToCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        try {
            Category category = categoryStorage.save(CategoryMapper.categoryFromNewCategoryDto(newCategoryDto));
            log.info("Добавлена категория {}", category);
            return categoryToCategoryDto(category);
        } catch (DataIntegrityViolationException e) {
            throw new CategoryAlreadyExistsException(
                    "Категория с названием '" + newCategoryDto.getName() + "' уже существует"
            );
        }
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(CategoryDto categoryDto) {
        int catId = categoryDto.getId();
        Optional<Category> categoryOptional = categoryStorage.findById(catId);
        if (categoryOptional.isEmpty())
            throw new CategoryNotFoundException("Категория с id=" + catId + " не найдена");
        Category oldCategory = categoryOptional.get();
        if (oldCategory.getName().equals(categoryDto.getName())) return categoryToCategoryDto(oldCategory);
        if (categoryStorage.existsByName(categoryDto.getName()))
            throw new CategoryAlreadyExistsException(
                    "Категория с названием '" + categoryDto.getName() + "' уже существует"
            );
        Category category = categoryStorage.save(CategoryMapper.categoryFromCategoryDto(categoryDto));
        log.info("Обновлена категория {}", category);
        return categoryToCategoryDto(category);
    }

    @Override
    public void deleteCategory(Integer catId) {
        if (!categoryStorage.existsById(catId))
            throw new CategoryNotFoundException("Категория с id=" + catId + " не найдена");
        try {
            categoryStorage.deleteById(catId);
            log.info("Удалена категория с id={}", catId);
        } catch (DataIntegrityViolationException e) {
            throw new OperationConditionsFailureException("Категория с id=" + catId + " не пуста");
        }
    }
}
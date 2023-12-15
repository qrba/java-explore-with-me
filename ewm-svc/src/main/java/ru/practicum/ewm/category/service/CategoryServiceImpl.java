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
import ru.practicum.ewm.category.storage.CategoryRepository;
import ru.practicum.ewm.event.storage.EventRepository;
import ru.practicum.ewm.exception.CategoryAlreadyExistsException;
import ru.practicum.ewm.exception.CategoryNotFoundException;
import ru.practicum.ewm.exception.OperationConditionsFailureException;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.ewm.category.model.dto.CategoryMapper.categoryFromNewCategoryDto;
import static ru.practicum.ewm.category.model.dto.CategoryMapper.categoryToCategoryDto;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Integer catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new CategoryNotFoundException("Категория с id=" + catId + " не найдена"));
        log.info("Запрошена категория {}", category);
        return categoryToCategoryDto(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        log.info("Запрошен список категорий");
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        return categoryRepository.findAll(pageable).stream()
                .map(CategoryMapper::categoryToCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        try {
            Category category = categoryRepository.save(categoryFromNewCategoryDto(newCategoryDto));
            log.info("Добавлена категория {}", category);
            return categoryToCategoryDto(category);
        } catch (DataIntegrityViolationException e) {
            throw new CategoryAlreadyExistsException(
                    "Категория с названием '" + newCategoryDto.getName() + "' уже существует"
            );
        }
    }

    @Override
    public CategoryDto updateCategory(CategoryDto categoryDto) {
        int catId = categoryDto.getId();
        String name = categoryDto.getName();
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new CategoryNotFoundException("Категория с id=" + catId + " не найдена"));
        if (categoryRepository.existsByNameAndIdNot(name, catId))
            throw new CategoryAlreadyExistsException(
                    "Категория с названием '" + name + "' уже существует"
            );
        category.setName(name);
        categoryRepository.save(category);
        log.info("Обновлена категория {}", category);
        return categoryToCategoryDto(category);
    }

    @Override
    public void deleteCategory(Integer catId) {
        if (!categoryRepository.existsById(catId))
            throw new CategoryNotFoundException("Категория с id=" + catId + " не найдена");
        if (eventRepository.countByCategoryId(catId) > 0)
            throw new OperationConditionsFailureException("Категория с id=" + catId + " не пуста");
        categoryRepository.deleteById(catId);
        log.info("Удалена категория с id={}", catId);
    }
}
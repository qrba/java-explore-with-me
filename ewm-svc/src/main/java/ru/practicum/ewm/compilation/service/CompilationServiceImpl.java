package ru.practicum.ewm.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.model.dto.CompilationDto;
import ru.practicum.ewm.compilation.model.dto.CompilationMapper;
import ru.practicum.ewm.compilation.model.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.model.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.storage.CompilationRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.storage.EventRepository;
import ru.practicum.ewm.exception.CompilationAlreadyExistsException;
import ru.practicum.ewm.exception.CompilationNotFoundException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.practicum.ewm.compilation.model.dto.CompilationMapper.compilationFromNewDto;
import static ru.practicum.ewm.compilation.model.dto.CompilationMapper.compilationToDto;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("Запрошен список подборок");
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        return compilationRepository.findByPinned(pinned, pageable).stream()
                .map(CompilationMapper::compilationToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Integer compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new CompilationNotFoundException("Подборка с id=" + compId + " не найдена"));
        log.info("Запрошена подборка {}", compilation);
        return compilationToDto(compilation);
    }

    @Override
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        try {
            List<Integer> ids = newCompilationDto.getEvents();
            List<Event> events = (ids == null) ? Collections.emptyList() : eventRepository.findAllById(ids);
            if (newCompilationDto.getPinned() == null) newCompilationDto.setPinned(false);
            Compilation compilation = compilationRepository.save(
                    compilationFromNewDto(newCompilationDto, Set.copyOf(events))
            );
            log.info("Добавлена подборка {}", compilation);
            return compilationToDto(compilation);
        } catch (DataIntegrityViolationException e) {
            throw new CompilationAlreadyExistsException(
                    "Подборка с заголовком '" + newCompilationDto.getTitle() + "' уже существует"
            );
        }
    }

    @Override
    public void deleteCompilation(Integer compId) {
        if (!compilationRepository.existsById(compId))
            throw new CompilationNotFoundException("Подборка с id=" + compId + " не найдена");
        compilationRepository.deleteById(compId);
        log.info("Удалена подборка с id={}", compId);
    }

    @Override
    public CompilationDto updateCompilation(UpdateCompilationRequest updateCompilationRequest, Integer compId) {
        String title = updateCompilationRequest.getTitle();
        Boolean pinned = updateCompilationRequest.getPinned();
        List<Integer> newEventIds = updateCompilationRequest.getEvents();
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new CompilationNotFoundException("Подборка с id=" + compId + " не найдена"));
        if (title != null) {
            if (
                    !title.equals(compilation.getTitle()) &&
                            compilationRepository.existsByTitle(title)
            )
                throw new CompilationAlreadyExistsException(
                        "Подборка с заголовком '" + updateCompilationRequest.getTitle() + "' уже существует"
                );
            compilation.setTitle(title);
        }
        if (pinned != null) compilation.setPinned(pinned);
        if (newEventIds != null) {
            List<Integer> oldEventIds = compilation.getEvents().stream().map(Event::getId).collect(Collectors.toList());
            if (!newEventIds.equals(oldEventIds)) {
                Set<Event> events = new HashSet<>(eventRepository.findAllById(newEventIds));
                compilation.setEvents(events);
            }
        }
        log.info("Обновлена подборка {}", compilation);
        return compilationToDto(compilationRepository.save(compilation));
    }
}
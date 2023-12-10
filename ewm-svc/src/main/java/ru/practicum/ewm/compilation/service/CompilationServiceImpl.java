package ru.practicum.ewm.compilation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.client.StatsClient;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.model.dto.CompilationDto;
import ru.practicum.ewm.compilation.model.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.model.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.storage.CompilationStorage;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.dto.EventShortDto;
import ru.practicum.ewm.event.storage.EventStorage;
import ru.practicum.ewm.exception.CompilationAlreadyExistsException;
import ru.practicum.ewm.exception.CompilationNotFoundException;
import ru.practicum.ewm.participationrequest.model.ParticipationRequestStatus;
import ru.practicum.ewm.participationrequest.storage.ParticipationRequestStorage;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.practicum.ewm.compilation.model.dto.CompilationMapper.compilationFromNewDto;
import static ru.practicum.ewm.compilation.model.dto.CompilationMapper.compilationToDto;
import static ru.practicum.ewm.event.model.dto.EventMapper.eventToShortDto;

@RequiredArgsConstructor
@Service
@Slf4j
public class CompilationServiceImpl implements CompilationService {
    private final CompilationStorage compilationStorage;
    private final EventStorage eventStorage;
    private final ParticipationRequestStorage requestStorage;
    private final StatsClient statsClient;

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("Запрошен список подборок с pinned=" + pinned);
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        return compilationStorage.findByPinned(pinned, pageable).stream()
                .map(
                        compilation -> compilationToDto(
                                compilation,
                                getEventShortDtoList(compilation)
                        )
                )
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Integer compId) {
        Optional<Compilation> compilationOptional = compilationStorage.findById(compId);
        if (compilationOptional.isEmpty())
            throw new CompilationNotFoundException("Категория с id=" + compId + " не найдена");
        Compilation compilation = compilationOptional.get();
        log.info("Запрошена подборка {}", compilation);
        return compilationToDto(compilation, getEventShortDtoList(compilation));
    }

    @Override
    @Transactional
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        try {
            List<Integer> ids = newCompilationDto.getEvents();
            List<Event> events = (ids == null) ? Collections.emptyList() : eventStorage.findAllById(ids);
            if (newCompilationDto.getPinned() == null) newCompilationDto.setPinned(false);
            Compilation compilation = compilationStorage.save(
                    compilationFromNewDto(newCompilationDto, Set.copyOf(events))
            );
            log.info("Добавлена подборка {}", compilation);
            return compilationToDto(compilation, getEventShortDtoList(compilation));
        } catch (DataIntegrityViolationException e) {
            throw new CompilationAlreadyExistsException(
                    "Подборка с названием '" + newCompilationDto.getTitle() + "' уже существует"
            );
        }
    }

    @Override
    @Transactional
    public void deleteCompilation(Integer compId) {
        if (!compilationStorage.existsById(compId))
            throw new CompilationNotFoundException("Подборка с id=" + compId + " не найдена");
        compilationStorage.deleteById(compId);
        log.info("Удалена подборка с id={}", compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(UpdateCompilationRequest updateCompilationRequest, Integer compId) {
        String title = updateCompilationRequest.getTitle();
        Boolean pinned = updateCompilationRequest.getPinned();
        List<Integer> newEventIds = updateCompilationRequest.getEvents();
        Optional<Compilation> compilationOptional = compilationStorage.findById(compId);
        if (compilationOptional.isEmpty())
            throw new CompilationNotFoundException("Подборка с id=" + compId + " не найдена");
        Compilation compilation = compilationOptional.get();
        if (title != null) compilation.setTitle(title);
        if (pinned != null) compilation.setPinned(pinned);
        if (newEventIds != null) {
            List<Integer> oldEventIds = compilation.getEvents().stream().map(Event::getId).collect(Collectors.toList());
            if (!newEventIds.equals(oldEventIds)) {
                Set<Event> events = new HashSet<>(eventStorage.findAllById(newEventIds));
                compilation.setEvents(events);
            }
        }
        log.info("Обновлена подборка {}", compilation);
        return compilationToDto(compilationStorage.save(compilation), getEventShortDtoList(compilation));
    }

    private List<EventShortDto> getEventShortDtoList(Compilation compilation) {
        Set<Event> events = compilation.getEvents();
        Map<Integer, Long> hits = new HashMap<>();
        Optional<LocalDateTime> earliestPublishedOptional = events.stream()
                .filter(event -> event.getState().equals(EventState.PUBLISHED))
                .map(Event::getPublishedOn)
                .min(LocalDateTime::compareTo);
        if (earliestPublishedOptional.isPresent()) {
            List<String> uris = events.stream()
                    .map(event -> String.format("/events/%s", event.getId()))
                    .collect(Collectors.toList());
            ResponseEntity<Object> response = statsClient.getStats(
                    earliestPublishedOptional.get(),
                    LocalDateTime.now(),
                    uris,
                    true
            );
            ObjectMapper mapper = new ObjectMapper();
            List<ViewStatsDto> statsDto = mapper.convertValue(response.getBody(), new TypeReference<>() {});
            for (ViewStatsDto stat : statsDto)
                hits.put(Integer.parseInt(stat.getUri().substring(8)), stat.getHits());
        }
        return events.stream()
                .map(
                        event -> {
                            if (event.getState().equals(EventState.PUBLISHED)) {
                                int id = event.getId();
                                return eventToShortDto(
                                        event,
                                        requestStorage.countByStatusAndEventId(
                                                ParticipationRequestStatus.CONFIRMED,
                                                id
                                        ),
                                        hits.getOrDefault(id, 0L)
                                );
                            } else {
                                return eventToShortDto(event, 0, 0L);
                            }
                        }
                )
                .collect(Collectors.toList());
    }
}
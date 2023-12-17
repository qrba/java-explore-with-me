package ru.practicum.ewm.compilation.model.dto;

import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.event.model.Event;

import java.util.Set;
import java.util.stream.Collectors;

import static ru.practicum.ewm.event.model.dto.EventMapper.eventToShortDto;

public class CompilationMapper {
    public static CompilationDto compilationToDto(Compilation compilation) {
        return new CompilationDto(
                compilation.getId(),
                compilation.getPinned(),
                compilation.getTitle(),
                compilation.getEvents().stream()
                        .map(event -> eventToShortDto(event, null, null))
                        .collect(Collectors.toList())
        );
    }

    public static Compilation compilationFromNewDto(NewCompilationDto newCompilationDto, Set<Event> events) {
        return new Compilation(
                null,
                newCompilationDto.getPinned(),
                newCompilationDto.getTitle(),
                events
        );
    }
}
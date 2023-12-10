package ru.practicum.ewm.compilation.model.dto;

import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.dto.EventShortDto;

import java.util.List;
import java.util.Set;

public class CompilationMapper {
    public static CompilationDto compilationToDto(Compilation compilation, List<EventShortDto> eventShortDtos) {
        return new CompilationDto(
                compilation.getId(),
                compilation.getPinned(),
                compilation.getTitle(),
                eventShortDtos
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
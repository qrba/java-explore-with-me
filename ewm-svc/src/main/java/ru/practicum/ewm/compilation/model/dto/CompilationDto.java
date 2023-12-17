package ru.practicum.ewm.compilation.model.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.event.model.dto.EventShortDto;

import java.util.List;

@Data
@RequiredArgsConstructor
public class CompilationDto {
    private final Integer id;
    private final Boolean pinned;
    private final String title;
    private final List<EventShortDto> events;
}
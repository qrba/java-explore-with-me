package ru.practicum.ewm.compilation.service;

import ru.practicum.ewm.compilation.model.dto.CompilationDto;
import ru.practicum.ewm.compilation.model.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.model.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    List<CompilationDto> getCompilations(
            Boolean pinned,
            Integer from,
            Integer size
    );

    CompilationDto getCompilationById(Integer compId);

    CompilationDto addCompilation(NewCompilationDto newCompilationDto);

    void deleteCompilation(Integer compId);

    CompilationDto updateCompilation(UpdateCompilationRequest updateCompilationRequest, Integer compId);
}
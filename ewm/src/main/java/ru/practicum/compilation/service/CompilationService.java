package ru.practicum.compilation.service;

import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.ShortCompilationDto;

import java.util.List;

public interface CompilationService {
    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);

    CompilationDto getCompilation(Long id);

    CompilationDto createCompilation(ShortCompilationDto compilationDto);

    void deleteCompilation(Long id);

    CompilationDto patch(Long compId, ShortCompilationDto compilationDto);

}
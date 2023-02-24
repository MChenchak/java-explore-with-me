package ru.practicum.compilation.service;

import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.ShortCompilationDto;

import java.util.List;

public interface CompilationService {
    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);

    CompilationDto getCompilation(Long id);

    CompilationDto createCompilation(ShortCompilationDto compilationDto);

    void deleteCompilation(Long id);

    void deleteEventFromCompilation(Long id, Long eventId);

    void addEventToCompilation(Long id, Long eventId);

    void deleteCompilationFromMainPage(Long id);

    void addCompilationToMainPage(Long id);
}
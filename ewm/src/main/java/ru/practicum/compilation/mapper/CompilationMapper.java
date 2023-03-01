package ru.practicum.compilation.mapper;

import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.ShortCompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.mapper.EventMapper;

import java.util.stream.Collectors;

public class CompilationMapper {
    public static Compilation toCompilation(CompilationDto compilationDto) {
        return Compilation
                .builder()
                .id(compilationDto.getId())
                .title(compilationDto.getTitle())
                .pinned(compilationDto.getPinned())
                .events(compilationDto.getEvents().stream().map(EventMapper::toEvent).collect(Collectors.toList()))
                .build();
    }

    public static CompilationDto toCompilationDto(Compilation compilation) {
        return CompilationDto
                .builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(compilation.getEvents().stream().map(EventMapper::toShortEventDto).collect(Collectors.toList()))
                .build();
    }

    public static Compilation toCompilation(ShortCompilationDto compilationDto) {
        return Compilation
                .builder()
                .title(compilationDto.getTitle())
                .pinned(compilationDto.getPinned())
                .build();
    }
}

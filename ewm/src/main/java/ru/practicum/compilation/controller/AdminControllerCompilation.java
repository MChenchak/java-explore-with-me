package ru.practicum.compilation.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.ShortCompilationDto;
import ru.practicum.compilation.service.CompilationService;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/admin/compilations")
public class AdminControllerCompilation {
    private final CompilationService compilationService;

    public AdminControllerCompilation(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilation(@Valid @RequestBody ShortCompilationDto compilationDto) {
        log.info("create new compilation");
        return compilationService.createCompilation(compilationDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long id) {
        log.info("delete compilation with id {}", id);
        compilationService.deleteCompilation(id);
    }

    @PatchMapping("/{compId}")
    public CompilationDto patchCompilation(@PathVariable Long compId,
                                                @Valid @RequestBody ShortCompilationDto compilationDto) {
        log.info("patch compilation with id {}", compId);
        return compilationService.patch(compId, compilationDto);
    }
}

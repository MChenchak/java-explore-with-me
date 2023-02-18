package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.model.ViewStats;
import ru.practicum.service.HitService;

import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Validated
public class StatController {
    private final HitService hitService;

    @GetMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    public List<ViewStats> getStats(@RequestParam("start") @NotEmpty String start,
                                    @RequestParam("end")  @NotEmpty String end,
                                    @RequestParam("uris") @NotEmpty List<String> uris,
                                    @RequestParam(required = false) boolean unique) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDT = LocalDateTime.parse(start, formatter);
        LocalDateTime endDT = LocalDateTime.parse(end, formatter);
        return hitService.getHitStat(startDT, endDT, uris, unique);
    }

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void save(@NotNull @RequestBody EndpointHitDto dto) {
        hitService.save(dto);
    }
}

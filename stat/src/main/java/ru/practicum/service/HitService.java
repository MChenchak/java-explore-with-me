package ru.practicum.service;

import ru.practicum.dto.EndpointHitDto;
import ru.practicum.model.Hit;
import ru.practicum.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;


public interface HitService {
    List<ViewStats> getHitStat(LocalDateTime start,
                               LocalDateTime end,
                               List<String> uris,
                               boolean unique);

    void save (EndpointHitDto endpointHitDto);
}

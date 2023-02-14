package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.HitMapper;
import ru.practicum.model.Hit;
import ru.practicum.model.ViewStats;
import ru.practicum.repository.HitRepository;

import javax.persistence.Tuple;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HitServiceImpl implements HitService {

    private final HitRepository hitRepository;

    private final ModelMapper modelMapper = new ModelMapper();

    @Override
    public List<ViewStats> getHitStat(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        List<Tuple> results  = hitRepository.searchHit(start, end, uris, unique)
                .orElseThrow(() -> new NotFoundException("Data not found"));

        List<ViewStats> stats = results.stream()
                .map(r -> {
                    Map<String, Object> maps = new HashMap<>();
                    r.getElements().forEach(tupleElement -> {
                        maps.put(tupleElement.getAlias(), r.get(tupleElement.getAlias()));
                    });
                    return modelMapper.map(maps, ViewStats.class);
                }).collect(Collectors.toList());

        return stats;
    }

    @Override
    @Transactional
    public void save(EndpointHitDto endpointHitDto) {
        Hit hit = HitMapper.endpointHitDtoToHit(endpointHitDto);
        hitRepository.save(hit);
    }
}

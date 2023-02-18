package ru.practicum.repository;

import ru.practicum.model.Hit;

import javax.persistence.Tuple;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HitRepository {

    Optional<List<Tuple>> searchHit(LocalDateTime start,
                          LocalDateTime end,
                          List<String> uris,
                          boolean distinct);

    void save(Hit entity);

}

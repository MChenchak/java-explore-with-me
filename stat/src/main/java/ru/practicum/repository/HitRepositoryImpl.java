package ru.practicum.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.model.Hit;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class HitRepositoryImpl implements HitRepository {
    @PersistenceContext
    private final EntityManager em;

    @Override
    public Optional<List<Tuple>> searchHit(LocalDateTime start, LocalDateTime end, List<String> uris, boolean distinct) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> tupleQuery = cb.createTupleQuery();

        Root<Hit> hit = tupleQuery.from(Hit.class);
        Predicate betweenDate = cb.between(hit.get("timestamp"), start, end);

        Expression<Long> count = distinct ? cb.countDistinct(hit.get("ip")) : cb.count(hit.get("ip"));

        tupleQuery
                .select(cb.tuple(hit.get("app").alias("app"),
                        hit.get("uri").alias("uri"),
                        count.alias("hits")))
                .where(hit.get("uri").in(uris), betweenDate)
                .groupBy(hit.get("app"), hit.get("uri"));

        return Optional.ofNullable(em.createQuery(tupleQuery).getResultList());
    }

    @Override
    public void save(Hit entity) {
        em.persist(entity);
    }
}

package ru.practicum.participation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.participation.model.Participation;
import ru.practicum.participation.model.StatusRequest;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    List<Participation> findAllByRequesterId(Long userId);

    Participation findByEventIdAndRequesterId(Long eventId, Long userId);

    int countParticipationByEventIdAndStatus(Long eventId, StatusRequest status);

    List<Participation> findAllByEventIdAndEventInitiatorId(Long eventId, Long userId);

    @Query("select p from Participation p  where p.id = ?1 and p.requester.id = ?2 " +
            "order by p.created asc")
    Participation canselParticipationRequest(Long reqId, Long userId);
}

package ru.practicum.participation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.participation.model.Participation;
import ru.practicum.participation.model.StatusRequest;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    List<Participation> findAllByRequesterId(Long userId);

    List<Participation> findAllByEventId(Long eventId);

    Participation findByEventIdAndRequesterId(Long eventId, Long userId);

    int countParticipationByEventIdAndStatus(Long eventId, StatusRequest status);

    List<Participation> findAllByEventIdAndEventInitiatorId(Long eventId, Long userId);

    @Query("select p from Participation p  where p.id = ?1 and p.requester.id = ?2 " +
            "order by p.created asc")
    Participation canselParticipationRequest(Long reqId, Long userId);

    @Query(value = "SELECT r FROM Participation r WHERE r.event.id = :eventId AND r.id IN :requestIds")
    List<Participation> findStoredUpdRequests(@Param("eventId") Long eventId, @Param("requestIds") List<Long> ids);

    @Query(value = "SELECT r FROM Participation r WHERE r.status = :status AND r.id IN :ids")
    List<Participation> findStoredUpdRequestsWithStatus(@Param("status") StatusRequest status, @Param("ids") List<Long> ids);

}

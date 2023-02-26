package ru.practicum.participation.service;

import ru.practicum.event.dto.RequestStatusUpdateDto;
import ru.practicum.participation.dto.ParticipationDto;
import ru.practicum.participation.dto.RequestListDto;

import java.util.List;

public interface ParticipationService {
    List<ParticipationDto> getParticipationRequests(Long userId);

    ParticipationDto createParticipationRequest(Long userId, Long eventId);

    ParticipationDto cancelParticipationRequest(Long userId, Long reqId);

    List<ParticipationDto> getParticipationRequests(Long eventId, Long userId);

    RequestListDto updateRequestsStatusForEvent(Long eventId, Long userId, RequestStatusUpdateDto dto);

}

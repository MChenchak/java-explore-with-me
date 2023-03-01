package ru.practicum.participation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.dto.RequestStatusUpdateDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.participation.dto.ParticipationDto;
import ru.practicum.participation.dto.RequestListDto;
import ru.practicum.participation.mapper.ParticipationMapper;
import ru.practicum.participation.model.Participation;
import ru.practicum.participation.model.StatusRequest;
import ru.practicum.participation.repository.ParticipationRepository;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.event.model.State.PUBLISHED;
import static ru.practicum.participation.model.StatusRequest.*;

@Service
@Transactional
public class ParticipationServiceImpl implements ParticipationService {
    private final ParticipationRepository participationRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public ParticipationServiceImpl(ParticipationRepository participationRepository,
                                    UserRepository userRepository,
                                    EventRepository eventRepository) {
        this.participationRepository = participationRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationDto> getParticipationRequests(Long userId) {
        return participationRepository.findAllByRequesterId(userId)
                .stream()
                .map(ParticipationMapper::toParticipationDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationDto createParticipationRequest(Long userId, Long eventId) {
        if (participationRepository.findByEventIdAndRequesterId(eventId, userId) != null) {
            throw new BadRequestException("participation request already exist");
        }
        Participation participation = Participation
                .builder()
                .created(LocalDateTime.now())
                .requester(userRepository.findById(userId)
                        .orElseThrow(() -> new NotFoundException("user with id = " + userId + " not found")))
                .event(eventRepository.findById(eventId)
                        .orElseThrow(() -> new NotFoundException("event with id = " + eventId + " not found")))
                .status(CONFIRMED)
                .build();
        if (userId.equals(participation.getEvent().getInitiator().getId())) {
            throw new BadRequestException("requester can`t be initiator of event");
        }
        if (!participation.getEvent().getState().equals(PUBLISHED)) {
            throw new BadRequestException("event not published");
        }
        if (participation.getEvent().getParticipantLimit() <= participationRepository
                .countParticipationByEventIdAndStatus(eventId, CONFIRMED)) {
            throw new BadRequestException("the limit of requests for participation has been exhausted");
        }
        if (Boolean.TRUE.equals(participation.getEvent().getRequestModeration())) {
            participation.setStatus(PENDING);
        }
        return ParticipationMapper.toParticipationDto(participationRepository.save(participation));
    }

    @Override
    public ParticipationDto cancelParticipationRequest(Long userId, Long reqId) {
        Participation participation = participationRepository.canselParticipationRequest(reqId, userId);
        if (participation == null)
            throw new NotFoundException("Request with id=" + reqId + " was not found");
        participation.setStatus(CANCELED);
        return ParticipationMapper.toParticipationDto(participationRepository.save(participation));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationDto> getParticipationRequests(Long eventId, Long userId) {
        return participationRepository.findAllByEventIdAndEventInitiatorId(eventId, userId)
                .stream()
                .map(ParticipationMapper::toParticipationDto)
                .collect(Collectors.toList());
    }

    public RequestListDto updateRequestsStatusForEvent(Long eventId, Long userId, RequestStatusUpdateDto dto) {
        Event storedEvent = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие с id" + eventId + "не найдено"));
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id" + userId + "не найден"));
        List<Participation> requestsForUpdate = participationRepository.findStoredUpdRequests(eventId, dto.getRequestIds());
        checkRequestsListForUpdate(dto.getStatus(), storedEvent, requestsForUpdate);
        eventRepository.save(storedEvent);
        return createRequestListDto(dto.getRequestIds());
    }

    private void checkRequestsListForUpdate(StatusRequest newStatus,
                                            Event storedEvent, List<Participation> requestsForUpdate) {
        for (Participation request : requestsForUpdate) {
            if (storedEvent.getParticipantLimit() == 0) {
                request.setStatus(StatusRequest.REJECTED);
                participationRepository.save(request);
                throw new BadRequestException("Мест нет");
            }
            if (!request.getStatus().equals(StatusRequest.PENDING)) {
                throw new BadRequestException("Запрос не в ожидании");
            }
            if (newStatus.equals(StatusRequest.CONFIRMED)) {
                request.setStatus(StatusRequest.CONFIRMED);
                participationRepository.save(request);
                storedEvent.setParticipantLimit(storedEvent.getParticipantLimit() - 1);
            }
            if (newStatus.equals(StatusRequest.REJECTED)) {
                request.setStatus(StatusRequest.REJECTED);
                participationRepository.save(request);
            }
        }
    }

    private RequestListDto createRequestListDto(List<Long> idRequests) {
        List<ParticipationDto> confirmedRequests = participationRepository.findStoredUpdRequestsWithStatus(StatusRequest.CONFIRMED,
                        idRequests)
                .stream()
                .map(ParticipationMapper::toParticipationDto)
                .collect(Collectors.toList());
        List<ParticipationDto> rejectedRequests = participationRepository.findStoredUpdRequestsWithStatus(StatusRequest.REJECTED,
                        idRequests)
                .stream()
                .map(ParticipationMapper::toParticipationDto)
                .collect(Collectors.toList());
        return new RequestListDto(confirmedRequests, rejectedRequests);
    }


}
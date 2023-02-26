package ru.practicum.event.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.*;
import ru.practicum.event.service.EventService;
import ru.practicum.participation.dto.ParticipationDto;
import ru.practicum.participation.dto.RequestListDto;
import ru.practicum.participation.service.ParticipationService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/events")
public class UserEventController {
    private final EventService eventService;
    private final ParticipationService participationService;

    public UserEventController(EventService eventService, ParticipationService participationService) {
        this.eventService = eventService;
        this.participationService = participationService;
    }

    @GetMapping
    public List<ShortEventDto> getUserEvents(@PathVariable Long userId,
                                             @RequestParam(defaultValue = "0") int from,
                                             @RequestParam (defaultValue = "10") int size) {
        log.info("get events added by user with id {}", userId);
        return eventService.getUserEvents(userId, from, size);
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventDto updateEvent(@PathVariable Long userId,
                                @PathVariable Long eventId,
                                @RequestBody UserUpdateEventDto eventDto) {
        log.info("update event by owner with id {}", userId);
        return eventService.updateEvent(userId, eventId, eventDto);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto createEvent(@PathVariable Long userId,
                                @Valid @RequestBody NewEventDto eventDto) {
        log.info("create event by user with id {}", userId);
        return eventService.createEvent(userId, eventDto);
    }

    @GetMapping("/{eventId}")
    public EventDto getEventByUser(@PathVariable Long userId,
                                   @PathVariable Long eventId) {
        log.info("get event with id {} by owner with id {}", eventId, userId);
        return eventService.getEventByUser(eventId, userId);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationDto> getParticipationRequests(@PathVariable Long userId,
                                                           @PathVariable Long eventId) {
        log.info("get participation requests by owner {} of event with id {}", userId, eventId);
        return participationService.getParticipationRequests(eventId, userId);
    }

    @PatchMapping("/{eventId}/requests")
    public RequestListDto patchRequestsState(@PathVariable Long userId,
                                                      @PathVariable Long eventId,
                                                      @RequestBody RequestStatusUpdateDto dto) {
        return participationService.updateRequestsStatusForEvent(eventId, userId, dto);
    }


}

package ru.practicum.event.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.AdminUpdateEventDto;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.service.EventService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/events")
public class AdminEventController {
    private final EventService eventService;

    public AdminEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventDto> getEventsByAdmin(@RequestParam(required = false) List<Long> users,
                                           @RequestParam(required = false) List<String> states,
                                           @RequestParam(required = false) List<Long> categories,
                                           @RequestParam(required = false) String rangeStart,
                                           @RequestParam(required = false) String rangeEnd,
                                           @RequestParam (defaultValue = "0") int from,
                                           @RequestParam (defaultValue = "10") int size) {
        log.info("get events by admin with param: userIds = {}, states = {}, categoryIds = {}, rangeStart = {}, " +
                "rangeEnd = {}, from = {}, size = {}", users, states, categories, rangeStart, rangeEnd, from, size);
        return eventService.getEventsByAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventDto updateEventByAdmin(@PathVariable Long eventId,
                                       @RequestBody AdminUpdateEventDto eventDto) {
        log.info("update event with id {} by admin", eventId);
        return eventService.updateEventByAdmin(eventId, eventDto);
    }

//    @PatchMapping("/{eventId}/publish")
//    public EventDto publishEvent(@PathVariable Long eventId) {
//        log.info("publish event with id {} by admin", eventId);
//        return eventService.publishEvent(eventId);
//    }

//    @PatchMapping("/{eventId}/reject")
//    public EventDto rejectEvent(@PathVariable Long eventId) {
//        log.info("reject event with id {} by admin", eventId);
//        return eventService.rejectEvent(eventId);
//    }
}

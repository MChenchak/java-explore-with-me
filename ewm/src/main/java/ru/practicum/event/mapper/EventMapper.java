package ru.practicum.event.mapper;

import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.ShortEventDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;

import java.time.LocalDateTime;

import static ru.practicum.category.mapper.CategoryMapper.toCategory;
import static ru.practicum.category.mapper.CategoryMapper.toCategoryDto;
import static ru.practicum.event.mapper.LocationMapper.toLocationDto;
import static ru.practicum.user.Constant.DATE_TIME_FORMATTER;
import static ru.practicum.user.mapper.UserMapper.toShortUserDto;

public class EventMapper {

    private EventMapper() {
    }

    public static EventDto toEventDto(Event event) {
        return EventDto
                .builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(toCategoryDto(event.getCategory()))
                .createdOn(event.getCreatedOn().format(DATE_TIME_FORMATTER))
                .description(event.getDescription())
                .eventDate(event.getEventDate().format(DATE_TIME_FORMATTER))
                .initiator(toShortUserDto(event.getInitiator()))
                .location(toLocationDto(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }

    public static Event toEvent(EventDto eventDto) {
        return Event
                .builder()
                .id(eventDto.getId())
                .annotation(eventDto.getAnnotation())
                .category(toCategory(eventDto.getCategory()))
                .createdOn(LocalDateTime.parse(eventDto.getCreatedOn(),
                        DATE_TIME_FORMATTER))
                .description(eventDto.getDescription())
                .eventDate(LocalDateTime.parse(eventDto.getEventDate(),
                        DATE_TIME_FORMATTER))
                .paid(eventDto.getPaid())
                .participantLimit(eventDto.getParticipantLimit())
                .publishedOn(eventDto.getPublishedOn())
                .requestModeration(eventDto.getRequestModeration())
                .state(eventDto.getState())
                .title(eventDto.getTitle())
                .build();
    }

    public static ShortEventDto toShortEventDto(Event event) {
        return ShortEventDto
                .builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(toCategoryDto(event.getCategory()))
                .eventDate(event.getEventDate().format(DATE_TIME_FORMATTER))
                .initiator(toShortUserDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }

    public static Event toEvent(ShortEventDto shortEventDto) {
        return Event
                .builder()
                .id(shortEventDto.getId())
                .annotation(shortEventDto.getAnnotation())
                .category(toCategory(shortEventDto.getCategory()))
                .eventDate(LocalDateTime.parse(shortEventDto.getEventDate(),
                        DATE_TIME_FORMATTER))
                .paid(shortEventDto.getPaid())
                .title(shortEventDto.getTitle())
                .build();
    }

    public static Event toEvent(NewEventDto eventDto) {
        return Event
                .builder()
                .annotation(eventDto.getAnnotation())
                .description(eventDto.getDescription())
                .eventDate(LocalDateTime.parse(eventDto.getEventDate(),
                        DATE_TIME_FORMATTER))
                .paid(eventDto.getPaid())
                .participantLimit(eventDto.getParticipantLimit())
                .requestModeration(eventDto.getRequestModeration())
                .title(eventDto.getTitle())
                .state(State.PENDING)
                .createdOn(LocalDateTime.now())
                .build();
    }
}

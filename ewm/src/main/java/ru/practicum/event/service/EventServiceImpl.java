package ru.practicum.event.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;
import ru.practicum.event.model.State;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.participation.repository.ParticipationRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.event.model.State.*;
import static ru.practicum.event.model.StateAction.PUBLISH_EVENT;
import static ru.practicum.event.model.StateAction.REJECT_EVENT;
import static ru.practicum.participation.model.StatusRequest.CONFIRMED;
import static ru.practicum.user.Constant.DATE_TIME_FORMATTER;

@Service
@Transactional
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    public EventServiceImpl(EventRepository eventRepository,
                            ParticipationRepository participationRepository,
                            CategoryRepository categoryRepository,
                            LocationRepository locationRepository,
                            UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.participationRepository = participationRepository;
        this.categoryRepository = categoryRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShortEventDto> getEvents(String text, List<Long> categoryIds, Boolean paid, String rangeStart,
                                         String rangeEnd, Boolean onlyAvailable, String sort, int from, int size) {
        List<ShortEventDto> events = eventRepository.searchEvents(text, categoryIds, paid, PUBLISHED,
                        PageRequest.of(from / size, size))
                .stream()
                .filter(event -> rangeStart != null ?
                        event.getEventDate().isAfter(LocalDateTime.parse(rangeStart, DATE_TIME_FORMATTER)) :
                        event.getEventDate().isAfter(LocalDateTime.now())
                                && rangeEnd != null ? event.getEventDate().isBefore(LocalDateTime.parse(rangeEnd,
                                DATE_TIME_FORMATTER)) :
                                event.getEventDate().isBefore(LocalDateTime.MAX))
                .map(EventMapper::toShortEventDto)
                .map(this::setConfirmedRequests)
                .collect(Collectors.toList());
        if (Boolean.TRUE.equals(onlyAvailable)) {
            events = events.stream().filter(shortEventDto ->
                    shortEventDto.getConfirmedRequests() < eventRepository
                            .findById(shortEventDto.getId()).get().getParticipantLimit() ||
                            eventRepository.findById(shortEventDto.getId()).get().getParticipantLimit() == 0
            ).collect(Collectors.toList());
        }
        if (sort != null) {
            switch (sort) {
                case "EVENT_DATE":
                    events = events
                            .stream()
                            .sorted(Comparator.comparing(ShortEventDto::getEventDate))
                            .collect(Collectors.toList());
                    break;
                case "VIEWS":
                    events = events
                            .stream()
                            .sorted(Comparator.comparing(ShortEventDto::getViews))
                            .collect(Collectors.toList());
                    break;
                default:
                    throw new BadRequestException("can be sorted only by views or event date");
            }
        }
        return events
                .stream()
                .peek(shortEventDto -> incrementViews(shortEventDto.getId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventDto getEvent(Long id) {
        Event event = checkAndGetEvent(id);
        if (!event.getState().equals(PUBLISHED)) {
            throw new BadRequestException("event must be published");
        }
        incrementViews(id);
        return setConfirmedRequests(EventMapper.toEventDto(event));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShortEventDto> getUserEvents(Long userId, int from, int size) {
        User user = checkAndGetUser(userId);
        return eventRepository.findAllByInitiatorId(userId, PageRequest.of(from / size, size))
                .stream()
                .map(EventMapper::toShortEventDto)
                .map(this::setConfirmedRequests)
                .collect(Collectors.toList());
    }

    @Override
    public EventDto updateEvent(Long userId, UserUpdateEventDto eventDto) {
        Event event = checkAndGetEvent(eventDto.getEventId());
        if (!event.getInitiator().getId().equals(userId)) {
            throw new BadRequestException("only initiator can update event");
        }
        if (event.getState().equals(PUBLISHED)) {
            throw new BadRequestException("published event cant be update");
        }
        Optional.ofNullable(eventDto.getAnnotation()).ifPresent(event::setAnnotation);
        if (eventDto.getCategory() != null) {
            event.setCategory(categoryRepository.findById(eventDto.getCategory())
                    .orElseThrow(() -> new NotFoundException("category not found")));
        }
        Optional.ofNullable(eventDto.getDescription()).ifPresent(event::setDescription);
        if (eventDto.getEventDate() != null) {
            LocalDateTime date = LocalDateTime.parse(eventDto.getEventDate(),
                    DATE_TIME_FORMATTER);
            if (date.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new BadRequestException("date event is too late");
            }
            event.setEventDate(date);
        }
        Optional.ofNullable(eventDto.getPaid()).ifPresent(event::setPaid);
        Optional.ofNullable(eventDto.getParticipantLimit()).ifPresent(event::setParticipantLimit);
        Optional.ofNullable(eventDto.getTitle()).ifPresent(event::setTitle);
        if (event.getState().equals(CANCELED)) {
            event.setState(PENDING);
        }
        EventDto returnEventDto = EventMapper.toEventDto(eventRepository.save(event));
        return setConfirmedRequests(returnEventDto);
    }

    @Override
    public EventDto createEvent(Long userId, NewEventDto eventDto) {
        User user = checkAndGetUser(userId);
        Event event = EventMapper.toEvent(eventDto);
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("date event is too late");
        }
        Location location = locationRepository.save(LocationMapper.toLocation(eventDto.getLocation()));
        event.setCategory(categoryRepository.findById(eventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("category not found")));
        event.setLocation(location);
        event.setInitiator(user);
        return EventMapper.toEventDto(eventRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public EventDto getEventByUser(Long eventId, Long userId) {
        Event event = checkAndGetEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new BadRequestException("only initiator can get fullEventDto");
        }
        return setConfirmedRequests(EventMapper.toEventDto(event));
    }

    @Override
    public EventDto cancelEventByUser(Long eventId, Long userId) {
        Event event = checkAndGetEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new BadRequestException("only initiator of event can change it");
        }
        if (!event.getState().equals(PENDING)) {
            throw new BadRequestException("only pending event can be canceled");
        }
        event.setState(CANCELED);
        EventDto eventDto = EventMapper.toEventDto(eventRepository.save(event));
        return setConfirmedRequests(eventDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDto> getEventsByAdmin(List<Long> userIds, List<String> states, List<Long> categoryIds,
                                           String rangeStart, String rangeEnd, int from, int size) {
        List<State> stateList = states == null ? null : states
                .stream()
                .map(State::valueOf)
                .collect(Collectors.toList());
        return eventRepository.searchEventsByAdmin(userIds, stateList, categoryIds, PageRequest.of(from / size, size))
                .stream()
                .filter(event -> rangeStart != null ?
                        event.getEventDate().isAfter(LocalDateTime.parse(rangeStart, DATE_TIME_FORMATTER)) :
                        event.getEventDate().isAfter(LocalDateTime.now())
                                && rangeEnd != null ? event.getEventDate().isBefore(LocalDateTime.parse(rangeEnd,
                                DATE_TIME_FORMATTER)) : event.getEventDate().isBefore(LocalDateTime.MAX))
                .map(EventMapper::toEventDto)
                .map(this::setConfirmedRequests)
                .collect(Collectors.toList());
    }

    @Override
    public EventDto updateEventByAdmin(Long eventId, AdminUpdateEventDto eventDto) {
        Event event = checkAndGetEvent(eventId);
        Optional.ofNullable(eventDto.getAnnotation()).ifPresent(event::setAnnotation);
        if (eventDto.getCategory() != null) {
            event.setCategory(categoryRepository.findById(eventDto.getCategory())
                    .orElseThrow(() -> new NotFoundException("category not found")));
        }
        Optional.ofNullable(eventDto.getDescription()).ifPresent(event::setDescription);
        if (eventDto.getEventDate() != null) {
            event.setEventDate(LocalDateTime.parse(eventDto.getEventDate(), DATE_TIME_FORMATTER));
        }
        if (eventDto.getLocation() != null) {
            Location location = locationRepository.save(LocationMapper.toLocation(eventDto.getLocation()));
            event.setLocation(location);
        }

        if (Objects.equals(PUBLISH_EVENT, eventDto.getStateAction())) {
            event.setState(PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        }
        if (Objects.equals(REJECT_EVENT, eventDto.getStateAction())) {
            event.setState(CANCELED);
        }
        Optional.ofNullable(eventDto.getPaid()).ifPresent(event::setPaid);
        Optional.ofNullable(eventDto.getParticipantLimit()).ifPresent(event::setParticipantLimit);
        Optional.ofNullable(eventDto.getRequestModeration()).ifPresent(event::setRequestModeration);
        Optional.ofNullable(eventDto.getTitle()).ifPresent(event::setTitle);
        EventDto returnEventDto = EventMapper.toEventDto(eventRepository.save(event));
        return setConfirmedRequests(returnEventDto);
    }

    @Override
    public EventDto publishEvent(Long eventId) {
        Event event = checkAndGetEvent(eventId);
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new BadRequestException("event must start min after one hour of now");
        }
        if (!event.getState().equals(PENDING)) {
            throw new BadRequestException("state of event must be PENDING");
        }
        event.setState(PUBLISHED);
        EventDto eventDto = EventMapper.toEventDto(eventRepository.save(event));
        return setConfirmedRequests(eventDto);
    }

    @Override
    public EventDto rejectEvent(Long eventId) {
        Event event = checkAndGetEvent(eventId);
        event.setState(CANCELED);
        EventDto eventDto = EventMapper.toEventDto(eventRepository.save(event));
        return setConfirmedRequests(eventDto);
    }

    private EventDto setConfirmedRequests(EventDto eventDto) {
        eventDto.setConfirmedRequests(participationRepository.countParticipationByEventIdAndStatus(eventDto.getId(),
                CONFIRMED));
        return eventDto;
    }

    private ShortEventDto setConfirmedRequests(ShortEventDto eventDto) {
        eventDto.setConfirmedRequests(participationRepository.countParticipationByEventIdAndStatus(eventDto.getId(),
                CONFIRMED));
        return eventDto;
    }

    private void incrementViews(Long id) {
        Event event = checkAndGetEvent(id);
        long views = event.getViews() + 1;
        event.setViews(views);
    }

    private Event checkAndGetEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("event with id = " + id + " not found"));
    }

    private User checkAndGetUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user with id = " + id + " not found"));
    }
}

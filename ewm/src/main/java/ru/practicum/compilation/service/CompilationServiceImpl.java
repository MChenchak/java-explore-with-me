package ru.practicum.compilation.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.ShortCompilationDto;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.dto.ShortEventDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.participation.repository.ParticipationRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.participation.model.StatusRequest.CONFIRMED;

@Transactional
@Service
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;

    public CompilationServiceImpl(CompilationRepository compilationRepository,
                                  EventRepository eventRepository,
                                  ParticipationRepository participationRepository) {
        this.compilationRepository = compilationRepository;
        this.eventRepository = eventRepository;
        this.participationRepository = participationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        if (pinned == null) {
            return compilationRepository.findAll(PageRequest.of(from / size, size))
                    .stream()
                    .map(CompilationMapper::toCompilationDto)
                    .collect(Collectors.toList());
        }
        return compilationRepository.findAllByPinned(pinned, PageRequest.of(from / size, size))
                .stream()
                .map(CompilationMapper::toCompilationDto)
                .map(this::setViewsAndConfirmedRequests)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilation(Long id) {
        return setViewsAndConfirmedRequests(CompilationMapper.toCompilationDto(getAndCheckCompilation(id)));
    }

    @Override
    public CompilationDto createCompilation(ShortCompilationDto compilationDto) {
        Compilation compilation = CompilationMapper.toCompilation(compilationDto);
        List<Event> events = eventRepository.findAllById(compilationDto.getEvents());
        compilation.setEvents(events);
        return setViewsAndConfirmedRequests(CompilationMapper.toCompilationDto(compilationRepository.save(compilation)));
    }

    @Override
    public void deleteCompilation(Long id) {
        compilationRepository.delete(getAndCheckCompilation(id));
    }

    @Override
    public CompilationDto patch(Long compId, ShortCompilationDto compilationDto) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("compilation with id = " + compId + " not found"));

        Compilation newCompilation = createCompilationForUpdate(compilation, compilationDto);
        compilationRepository.save(newCompilation);
        return CompilationMapper.toCompilationDto(newCompilation);
    }


    private ShortEventDto setConfirmedRequests(ShortEventDto eventDto) {
        eventDto.setConfirmedRequests(participationRepository.countParticipationByEventIdAndStatus(eventDto.getId(),
                CONFIRMED));
        return eventDto;
    }

    private CompilationDto setViewsAndConfirmedRequests(CompilationDto compilationDto) {
        compilationDto.setEvents(compilationDto.getEvents()
                .stream()
                .map(this::setConfirmedRequests)
                .collect(Collectors.toList()));
        return compilationDto;
    }

    private Event getAndCheckEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("event with id = " + id + " not found"));
    }

    private Compilation getAndCheckCompilation(Long id) {
        return compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("compilation with id = " + id + " not found"));
    }

    private Compilation createCompilationForUpdate(Compilation stored, ShortCompilationDto updatingCompilationDto) {
        if (updatingCompilationDto.getPinned() != null) {
            stored.setPinned(updatingCompilationDto.getPinned());
        }
        if (updatingCompilationDto.getTitle() != null) {
            stored.setTitle(updatingCompilationDto.getTitle());
        }
        if (updatingCompilationDto.getEvents() != null) {
            stored.setEvents(eventRepository.findAllByEvents(updatingCompilationDto.getEvents()));
        }
        return stored;
    }
}

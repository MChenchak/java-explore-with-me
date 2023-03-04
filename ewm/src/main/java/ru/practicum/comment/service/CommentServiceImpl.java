package ru.practicum.comment.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.comment.model.CommentState.*;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public CommentServiceImpl(CommentRepository commentRepository, UserRepository userRepository, EventRepository eventRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public CommentDto createComment(CommentDto commentDto, Long userId, Long eventId) {
        User user = checkAndGetUser(userId);
        Event event = checkAndGetEvent(eventId);
        Comment comment = CommentMapper.toComment(commentDto);
        comment.setUser(user);
        comment.setEvent(event);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public CommentDto updateComment(Long commentId, Long userId, CommentDto commentDto) {
        Comment comment = commentRepository.findByIdAndUserId(commentId, userId)
                .orElseThrow(() -> new BadRequestException("Только автор может изменить комментарий"));
        comment.setText(commentDto.getText());
        comment.setState(NEW);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findByIdAndUserId(commentId, userId)
                .orElseThrow(() -> new BadRequestException("Только автор может удалить комментарий"));
        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllCommentsForEvent(Long eventId, int from, int size) {
        Event event = checkAndGetEvent(eventId);
        return commentRepository.findAllByEventAndState(event, APPROVED, PageRequest.of(from / size, size))
                .stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllCommentsByUser(Long userId, int from, int size) {
        User user = checkAndGetUser(userId);
        return commentRepository.findAllByUser(user, PageRequest.of(from / size, size))
                .stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto approveComment(Long commentId) {
        Comment comment = checkAndGetComment(commentId);
        comment.setState(APPROVED);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public CommentDto rejectComment(Long commentId) {
        Comment comment = checkAndGetComment(commentId);
        comment.setState(REJECTED);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private User checkAndGetUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user with id = " + id + " not found"));
    }

    private Event checkAndGetEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("event with id = " + id + " not found"));
    }

    private Comment checkAndGetComment(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("comment with id = " + id + " not found"));
    }
}

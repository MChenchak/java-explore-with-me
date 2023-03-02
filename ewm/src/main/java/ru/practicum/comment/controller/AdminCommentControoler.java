package ru.practicum.comment.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.service.CommentService;

import javax.validation.constraints.Positive;

@Slf4j
@RestController
@RequestMapping("/admin/comments")
public class AdminCommentControoler {
    private final CommentService commentService;

    public AdminCommentControoler(CommentService commentService) {
        this.commentService = commentService;
    }

    @PatchMapping("/{commentId}/approve")
    public CommentDto approveComment(@Positive @PathVariable Long commentId) {
        log.info("approve comment {}", commentId);
        return commentService.approveComment(commentId);
    }

    @PatchMapping("/{commentId}/reject")
    public CommentDto rejectComment(@Positive @PathVariable Long commentId) {
        log.info("reject comment {}", commentId);
        return commentService.rejectComment(commentId);
    }
}

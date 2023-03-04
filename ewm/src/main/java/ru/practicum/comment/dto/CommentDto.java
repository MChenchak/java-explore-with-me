package ru.practicum.comment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Builder
public class CommentDto {
    private Long id;

    @NotEmpty
    private String text;

    private String authorName;

    private String createdOn;
}
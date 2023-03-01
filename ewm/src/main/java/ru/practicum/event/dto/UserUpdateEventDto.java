package ru.practicum.event.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserUpdateEventDto {
    private Long eventId;

    private String annotation;

    private Long category;

    private String description;

    private String eventDate;

    private Boolean paid;

    private Integer participantLimit;

    private String title;

    private State stateAction;

    public enum State {
        SEND_TO_REVIEW,
        CANCEL_REVIEW
    }
}

package ru.practicum.event.dto;

import lombok.*;
import ru.practicum.event.model.StateAction;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUpdateEventDto {
    private String annotation;

    private Long category;

    private String description;

    private String eventDate;

    private LocationDto location;

    private Boolean paid;

    private Integer participantLimit;

    private Boolean requestModeration;

    private String title;

    private StateAction stateAction;
}

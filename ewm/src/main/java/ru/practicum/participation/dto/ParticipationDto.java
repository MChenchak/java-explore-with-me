package ru.practicum.participation.dto;

import lombok.*;
import ru.practicum.participation.model.StatusRequest;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationDto {
    private Long id;

    private String created;

    private Long event;

    private Long requester;

    private StatusRequest status;
}
package ru.practicum.participation.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestListDto {
    List<ParticipationDto> confirmedRequests;

    List<ParticipationDto> rejectedRequests;
}
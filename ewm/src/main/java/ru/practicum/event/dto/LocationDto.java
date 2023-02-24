package ru.practicum.event.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {
    private float lat;

    private float lon;
}

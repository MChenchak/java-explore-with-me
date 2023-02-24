package ru.practicum.user.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShortUserDto {
    private Long id;

    private String name;
}


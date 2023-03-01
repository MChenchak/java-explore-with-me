package ru.practicum.compilation.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShortCompilationDto {
    @NotBlank
    private String title;

    private Boolean pinned;

    private List<Long> events;
}

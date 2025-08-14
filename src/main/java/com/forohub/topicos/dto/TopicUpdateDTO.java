package com.forohub.topicos.dto;

import com.forohub.domain.TopicStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TopicUpdateDTO(
    @NotBlank @Size(max = 200) String title,
    @NotBlank String message,
    @NotNull TopicStatus status
) {}

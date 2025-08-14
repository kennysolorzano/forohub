package com.forohub.topicos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TopicCreateRequest(
        @NotBlank(message = "no debe estar vacío")
        @Size(max = 200, message = "longitud máxima 200")
        String title,

        @NotBlank(message = "no debe estar vacío")
        String message,

        @NotBlank(message = "no debe estar vacío")
        @Size(max = 120, message = "longitud máxima 120")
        String author,

        @NotBlank(message = "no debe estar vacío")
        @Size(max = 120, message = "longitud máxima 120")
        String course
) { }

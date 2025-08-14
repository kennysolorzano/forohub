package com.forohub.topicos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Para simplificar, hacemos un UPDATE completo (todas las propiedades requeridas).
 * Si prefieres un PATCH, dime y lo cambiamos a campos opcionales.
 */
public record TopicUpdateRequest(
        @NotBlank(message = "no debe estar vacío")
        @Size(max = 200, message = "longitud máxima 200")
        String title,

        @NotBlank(message = "no debe estar vacío")
        String message,

        @NotBlank(message = "no debe estar vacío")
        @Size(max = 120, message = "longitud máxima 120")
        String course
) { }

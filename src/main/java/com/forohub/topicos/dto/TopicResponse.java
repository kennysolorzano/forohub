package com.forohub.topicos.dto;

import java.time.LocalDateTime;

public record TopicResponse(
        Long id,
        String title,
        String message,
        String status,
        String author,
        String course,
        LocalDateTime createdAt
) { }

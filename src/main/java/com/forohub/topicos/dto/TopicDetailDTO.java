package com.forohub.topicos.dto;
public record TopicDetailDTO(
  Long id, String title, String message, String status,
  String author, String course, String createdAt
) {}

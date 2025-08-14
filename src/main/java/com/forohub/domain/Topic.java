package com.forohub.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "topics")
public class Topic {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private TopicStatus status = TopicStatus.OPEN;

    @Column(length = 120, nullable = false)
    private String author;

    @Column(length = 120, nullable = false)
    private String course;

    @Column(nullable = false)
    private boolean active = true;

    public Topic() {}

    public Topic(String title, String message, String author, String course) {
        this.title = title;
        this.message = message;
        this.author = author;
        this.course = course;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = TopicStatus.OPEN;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public TopicStatus getStatus() { return status; }
    public String getAuthor() { return author; }
    public String getCourse() { return course; }
    public boolean isActive() { return active; }

    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setStatus(TopicStatus status) { this.status = status; }
    public void setAuthor(String author) { this.author = author; }
    public void setCourse(String course) { this.course = course; }
    public void setActive(boolean active) { this.active = active; }
}

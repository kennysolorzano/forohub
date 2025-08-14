package com.forohub.service;

import com.forohub.domain.Topic;
import com.forohub.domain.TopicStatus;
import com.forohub.repository.TopicRepository;
import com.forohub.topicos.dto.TopicCreateRequest;
import com.forohub.topicos.dto.TopicResponse;
import com.forohub.topicos.dto.TopicUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;

    public Page<TopicResponse> list(Pageable pageable) {
        return topicRepository.findByActiveTrue(pageable)
                .map(this::toResponse);
    }

    public TopicResponse get(Long id) {
        Topic t = topicRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new EmptyResultDataAccessException("Topic not found", 1));
        return toResponse(t);
    }

    public TopicResponse create(TopicCreateRequest req) {
        Topic t = new Topic();
        t.setTitle(req.title());
        t.setMessage(req.message());
        t.setAuthor(req.author());
        t.setCourse(req.course());

        // 1) status es un enum
        t.setStatus(TopicStatus.OPEN);

        // 2) createdAt lo setea la BD o @PrePersist -> no llamar setCreatedAt()

        t.setActive(true);

        Topic saved = topicRepository.save(t);
        return toResponse(saved);
    }

    public TopicResponse update(Long id, TopicUpdateRequest req) {
        Topic t = topicRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new EmptyResultDataAccessException("Topic not found", 1));

        t.setTitle(req.title());
        t.setMessage(req.message());
        t.setCourse(req.course());

        Topic saved = topicRepository.save(t);
        return toResponse(saved);
    }

    /** Soft delete (active=false) */
    public void delete(Long id) {
        Topic t = topicRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new EmptyResultDataAccessException("Topic not found", 1));
        t.setActive(false);
        topicRepository.save(t);
    }

    private TopicResponse toResponse(Topic t) {
        // 3) TopicResponse espera status como String, convertimos el enum
        return new TopicResponse(
                t.getId(),
                t.getTitle(),
                t.getMessage(),
                t.getStatus() != null ? t.getStatus().name() : null,
                t.getAuthor(),
                t.getCourse(),
                t.getCreatedAt()
        );
    }
}

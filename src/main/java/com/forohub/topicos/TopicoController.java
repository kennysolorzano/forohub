package com.forohub.topicos;

import com.forohub.service.TopicService;
import com.forohub.topicos.dto.TopicCreateRequest;
import com.forohub.topicos.dto.TopicResponse;
import com.forohub.topicos.dto.TopicUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/topicos")
public class TopicoController {

    private final TopicService topicService;

    // GET /topicos (público)
    @GetMapping
    public ResponseEntity<Page<TopicResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(topicService.list(pageable));
    }

    // GET /topicos/{id} (público)
    @GetMapping("/{id}")
    public ResponseEntity<TopicResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(topicService.get(id));
    }

    // POST /topicos (protegido) -> 201 Created + Location
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<TopicResponse> create(@Valid @RequestBody TopicCreateRequest req) {
        TopicResponse created = topicService.create(req);
        URI location = URI.create("/topicos/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    // PUT /topicos/{id} (protegido)
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<TopicResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody TopicUpdateRequest req) {
        return ResponseEntity.ok(topicService.update(id, req));
    }

    // DELETE /topicos/{id} (protegido) -> 204 No Content
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        topicService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

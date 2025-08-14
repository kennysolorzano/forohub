package com.forohub.repository;

import com.forohub.domain.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    Page<Topic> findByActiveTrue(Pageable pageable);

    Optional<Topic> findByIdAndActiveTrue(Long id);
}

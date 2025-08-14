package com.forohub.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

/**
 * Fuerza a Spring Data a serializar Page<> con un formato estable (DTO),
 * eliminando el warning de PageImpl y asegurando un JSON consistente.
 */
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class SpringDataWebConfig {
}

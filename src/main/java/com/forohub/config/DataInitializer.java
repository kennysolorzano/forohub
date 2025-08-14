package com.forohub.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final JdbcTemplate jdbc;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            // Asegura admin si no existe
            Integer count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM users WHERE username = ?",
                    Integer.class, "admin"
            );
            if (count == null || count == 0) {
                // Si tu esquema tiene columnas distintas, ajusta los nombres del INSERT.
                // Aquí asumimos: username, password, role, enabled
                jdbc.update(
                        "INSERT INTO users (username, password, role, enabled) VALUES (?, ?, ?, ?)",
                        "admin", "{noop}admin123", "ROLE_ADMIN", true
                );
                log.info("Usuario admin creado por DataInitializer.");
            } else {
                log.info("Usuario admin ya existe; no se crea.");
            }
        } catch (Exception e) {
            // No abortamos el arranque si algo cambia en el esquema; solo informamos.
            log.warn("No se pudo verificar/crear usuario admin en DataInitializer: {}", e.getMessage());
        }
    }
}

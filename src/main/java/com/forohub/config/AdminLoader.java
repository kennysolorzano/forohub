package com.forohub.config;

import com.forohub.auth.User;
import com.forohub.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("default")
@RequiredArgsConstructor
public class AdminLoader implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminLoader.class);

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            User u = new User("admin", encoder.encode("admin123"), true);
            userRepository.save(u);
            log.info("Usuario admin creado");
        }
    }
}

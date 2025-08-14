package com.forohub.config;

import com.forohub.auth.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // habilita @PreAuthorize en el controlador
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Usa BCrypt en BD; si estás usando {noop} temporalmente, puedes cambiarlo,
        // pero lo recomendado es mantener BCrypt.
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider daoAuthProvider) {
        return new ProviderManager(daoAuthProvider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // No sesiones: puro JWT
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())

            // Autorización por rutas
            .authorizeHttpRequests(auth -> auth
                // Público: login para obtener token
                .requestMatchers(HttpMethod.POST, "/login").permitAll()
                // Público: lectura de tópicos
                .requestMatchers(HttpMethod.GET, "/topicos/**").permitAll()
                // Protegido: crear/editar/borrar tópicos
                .requestMatchers(HttpMethod.POST, "/topicos").authenticated()
                .requestMatchers(HttpMethod.PUT, "/topicos/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/topicos/**").authenticated()
                // Puedes decidir si /auth/me requiere autenticación (normalmente sí)
                .requestMatchers("/auth/me").authenticated()
                // Resto: permitir o proteger según necesites
                .anyRequest().permitAll()
            )

            // Filtro JWT antes del UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

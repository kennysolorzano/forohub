package com.forohub.topicos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TopicosApiIT {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private String bearer;

    @BeforeEach
    void login() throws Exception {
        String body = """
          {"username":"admin","password":"admin123"}
        """;
        var res = mvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        ).andExpect(status().isOk())
         .andReturn();

        JsonNode json = mapper.readTree(res.getResponse().getContentAsString());
        String token = json.get("token").asText();
        this.bearer = "Bearer " + token;
    }

    @Test
    @DisplayName("GET /topicos responde 200 y usa estructura estable de paginación")
    void list_usesStablePageDto() throws Exception {
        var result = mvc.perform(get("/topicos?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        JsonNode json = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.has("totalElements")).isTrue();
        assertThat(json.has("totalPages")).isTrue();
        assertThat(json.has("size")).isTrue();
        assertThat(json.has("number")).isTrue();
        assertThat(json.has("content")).isTrue();
        assertThat(json.get("content").isArray()).isTrue();
    }

    @Test
    @DisplayName("POST /topicos sin token -> 401/403")
    void post_withoutToken_isUnauthorizedOrForbidden() throws Exception {
        String nuevo = """
          {"title":"Titulo sin token","message":"m","author":"a","course":"c"}
        """;
        mvc.perform(post("/topicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(nuevo.getBytes(StandardCharsets.UTF_8)))
           .andExpect(result ->
               assertThat(result.getResponse().getStatus())
                   .isIn(401, 403)
           );
    }

    @Test
    @DisplayName("POST /topicos válido con token -> 201 + Location")
    void post_withToken_isCreated201() throws Exception {
        String unique = "Titulo IT " + System.nanoTime();
        String body = """
          {"title":"%s","message":"¿áéíóú? ñ","author":"Kenny","course":"Spring Boot"}
        """.formatted(unique);

        mvc.perform(post("/topicos")
                .header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.getBytes(StandardCharsets.UTF_8)))
           .andExpect(status().isCreated())
           .andExpect(header().exists("Location"))
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("POST /topicos inválido (Bean Validation) -> 400")
    void post_invalidBeanValidation_400() throws Exception {
        String invalido = """
          {"message":"x","author":"Kenny","course":"SB"}
        """;
        mvc.perform(post("/topicos")
                .header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalido.getBytes(StandardCharsets.UTF_8)))
           .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Flujo CRUD protegido: crear, obtener, actualizar, borrar")
    void crud_flow_ok() throws Exception {
        String title = "Flow IT " + System.currentTimeMillis();
        String create = """
          {"title":"%s","message":"m","author":"Kenny","course":"Spring Boot"}
        """.formatted(title);

        // CREATE -> 201
        var created = mvc.perform(post("/topicos")
                .header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(create.getBytes(StandardCharsets.UTF_8)))
           .andExpect(status().isCreated())
           .andReturn();

        JsonNode json = mapper.readTree(created.getResponse().getContentAsString());
        long id = json.get("id").asLong();

        // GET -> 200
        mvc.perform(get("/topicos/{id}", id))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id").value(id))
           .andExpect(jsonPath("$.title").value(title));

        // UPDATE -> 200
        String update = """
          {"title":"%s - edit","message":"mm","course":"Spring"}
        """.formatted(title);
        mvc.perform(put("/topicos/{id}", id)
                .header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(update.getBytes(StandardCharsets.UTF_8)))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.title").value(title + " - edit"))
           .andExpect(jsonPath("$.course").value("Spring"));

        // DELETE -> 204
        mvc.perform(delete("/topicos/{id}", id)
                .header("Authorization", bearer))
           .andExpect(status().isNoContent());
    }
}

package com.forohub.topicos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TopicosApiIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /topicos debe responder 200 sin autenticación")
    void listTopicsPublic_ok() throws Exception {
        mockMvc.perform(get("/topicos")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /topicos sin token debe responder 401 o 403")
    void createTopic_withoutToken_unauthorizedOrForbidden() throws Exception {
        String json = """
                {"title":"X","message":"Y","author":"Z","course":"Spring"}
                """;

        try {
            mockMvc.perform(post("/topicos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isUnauthorized());
        } catch (AssertionError e) {
            // Algunas configuraciones devuelven 403 en lugar de 401
            mockMvc.perform(post("/topicos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isForbidden());
        }
    }
}

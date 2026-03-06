package com.medicalstore.controller.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Actuator and Swagger/OpenAPI endpoints.
 * These endpoints should be accessible without authentication.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ActuatorSwaggerTest {

    @Autowired
    private MockMvc mockMvc;

    // ── Actuator ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Actuator Endpoints")
    class ActuatorEndpoints {

        @Test
        @DisplayName("/actuator/health — should be publicly accessible")
        void health_shouldBePublic() throws Exception {
            mockMvc.perform(get("/actuator/health")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("UP"));
        }

        @Test
        @DisplayName("/actuator/info — should return app info")
        void info_shouldReturnAppInfo() throws Exception {
            mockMvc.perform(get("/actuator/info")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("/actuator — should list available endpoints")
        void actuator_shouldListEndpoints() throws Exception {
            mockMvc.perform(get("/actuator")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._links.health").exists());
        }
    }

    // ── Swagger / OpenAPI ────────────────────────────────────────────────────

    @Nested
    @DisplayName("Swagger / OpenAPI Endpoints")
    class SwaggerEndpoints {

        @Test
        @DisplayName("/v3/api-docs — should return OpenAPI spec")
        void apiDocs_shouldBePublic() throws Exception {
            mockMvc.perform(get("/v3/api-docs")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.openapi").exists())
                    .andExpect(jsonPath("$.info.title").value("MedicalStore API"))
                    .andExpect(jsonPath("$.info.version").value("1.0.0"));
        }

        @Test
        @DisplayName("/swagger-ui.html — should redirect to Swagger UI")
        void swaggerUi_shouldBeAccessible() throws Exception {
            mockMvc.perform(get("/swagger-ui.html"))
                    .andExpect(status().is3xxRedirection());
        }
    }
}

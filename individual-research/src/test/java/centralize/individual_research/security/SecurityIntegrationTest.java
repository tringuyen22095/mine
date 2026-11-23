package centralize.individual_research.security;

import centralize.individual_research.repository.TerminologyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TerminologyRepository repository;

    @Test
    @DisplayName("Security: GET /public/terminology-store is accessible without authentication")
    void publicEndpoints_getAccessibleWithoutAuth() throws Exception {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/public/terminology-store"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Security: POST /public/terminology-store is accessible without authentication (unauthenticated write)")
    void publicEndpoints_postAccessibleWithoutAuth() throws Exception {
        String payload = """
                {
                    "category": "Cloud",
                    "keywords": ["firebase"],
                    "description": "Firestore store"
                }
                """;

        when(repository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/public/terminology-store")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Security: GET /public/terminology-store/search is accessible without authentication")
    void publicEndpoints_searchAccessibleWithoutAuth() throws Exception {
        when(repository.findByKeyword("firebase")).thenReturn(List.of());

        mockMvc.perform(get("/public/terminology-store/search").param("keyword", "firebase"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Security: Non-public endpoint /api/secured requires authentication and returns 401")
    void nonPublicEndpoints_requireAuth() throws Exception {
        mockMvc.perform(get("/api/secured"))
                .andExpect(status().isUnauthorized());
    }
}

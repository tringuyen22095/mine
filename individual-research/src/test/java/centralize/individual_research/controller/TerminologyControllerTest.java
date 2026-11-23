package centralize.individual_research.controller;

import centralize.individual_research.dto.TerminologyRequest;
import centralize.individual_research.dto.TerminologyResponse;
import centralize.individual_research.exception.GlobalExceptionHandler;
import centralize.individual_research.exception.ResourceNotFoundException;
import centralize.individual_research.security.SecurityConfig;
import centralize.individual_research.service.TerminologyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TerminologyController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class TerminologyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TerminologyService service;

    @Test
    @DisplayName("POST /public/terminology-store: creates record and returns 201 Created")
    void create_success() throws Exception {
        String payload = """
                {
                    "category": "Cloud",
                    "keywords": ["firestore", "nosql"],
                    "description": "Cloud Firestore document store"
                }
                """;

        TerminologyResponse response = TerminologyResponse.builder()
                .id("record-uuid-1")
                .category("Cloud")
                .keywords(List.of("firestore", "nosql"))
                .description("Cloud Firestore document store")
                .build();

        when(service.create(any(TerminologyRequest.class))).thenReturn(response);

        mockMvc.perform(post("/public/terminology-store")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("record-uuid-1")))
                .andExpect(jsonPath("$.category", is("Cloud")))
                .andExpect(jsonPath("$.keywords", hasSize(2)))
                .andExpect(jsonPath("$.description", is("Cloud Firestore document store")));
    }

    @Test
    @DisplayName("POST /public/terminology-store: returns 400 Bad Request on invalid payload")
    void create_invalidPayload() throws Exception {
        String payload = """
                {
                    "category": "",
                    "keywords": [],
                    "description": ""
                }
                """;

        mockMvc.perform(post("/public/terminology-store")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("Category is required")))
                .andExpect(jsonPath("$.path", is("/public/terminology-store")));
    }

    @Test
    @DisplayName("GET /public/terminology-store: returns 200 OK with all records")
    void getAll_success() throws Exception {
        TerminologyResponse response = TerminologyResponse.builder()
                .id("record-uuid-1")
                .category("Cloud")
                .keywords(List.of("firebase"))
                .description("Firebase overview")
                .build();

        when(service.getAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/public/terminology-store"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("record-uuid-1")))
                .andExpect(jsonPath("$[0].category", is("Cloud")));
    }

    @Test
    @DisplayName("GET /public/terminology-store/search?keyword=java: returns 200 OK with matching records")
    void search_success() throws Exception {
        TerminologyResponse response = TerminologyResponse.builder()
                .id("record-uuid-2")
                .category("Language")
                .keywords(List.of("java", "jvm"))
                .description("Java programming language")
                .build();

        when(service.searchByKeyword("java")).thenReturn(List.of(response));

        mockMvc.perform(get("/public/terminology-store/search")
                        .param("keyword", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].keywords[0]", is("java")));
    }

    @Test
    @DisplayName("GET /public/terminology-store/search without keyword param returns 400 Bad Request")
    void search_missingParam() throws Exception {
        mockMvc.perform(get("/public/terminology-store/search"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")));
    }

    @Test
    @DisplayName("GET /public/terminology-store/{id}: returns 200 OK when found")
    void getById_found() throws Exception {
        TerminologyResponse response = TerminologyResponse.builder()
                .id("record-uuid-1")
                .category("Cloud")
                .keywords(List.of("firebase"))
                .description("Firebase overview")
                .build();

        when(service.getById("record-uuid-1")).thenReturn(response);

        mockMvc.perform(get("/public/terminology-store/record-uuid-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("record-uuid-1")));
    }

    @Test
    @DisplayName("GET /public/terminology-store/{id}: returns 404 Not Found when missing")
    void getById_notFound() throws Exception {
        when(service.getById("missing-id")).thenThrow(new ResourceNotFoundException("Record not found with ID: missing-id"));

        mockMvc.perform(get("/public/terminology-store/missing-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("missing-id")));
    }

    @Test
    @DisplayName("PUT /public/terminology-store/{id}: updates and returns 200 OK")
    void update_success() throws Exception {
        String payload = """
                {
                    "category": "Database",
                    "keywords": ["sql", "relational"],
                    "description": "Relational database"
                }
                """;

        TerminologyResponse response = TerminologyResponse.builder()
                .id("record-uuid-1")
                .category("Database")
                .keywords(List.of("sql", "relational"))
                .description("Relational database")
                .build();

        when(service.update(eq("record-uuid-1"), any(TerminologyRequest.class))).thenReturn(response);

        mockMvc.perform(put("/public/terminology-store/record-uuid-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("record-uuid-1")))
                .andExpect(jsonPath("$.category", is("Database")));
    }

    @Test
    @DisplayName("PUT /public/terminology-store/{id}: returns 404 Not Found when ID does not exist")
    void update_notFound() throws Exception {
        String payload = """
                {
                    "category": "Database",
                    "keywords": ["sql"],
                    "description": "Relational database"
                }
                """;

        when(service.update(eq("missing-id"), any(TerminologyRequest.class)))
                .thenThrow(new ResourceNotFoundException("Record not found with ID: missing-id"));

        mockMvc.perform(put("/public/terminology-store/missing-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("DELETE /public/terminology-store/{id}: returns 204 No Content on success")
    void delete_success() throws Exception {
        doNothing().when(service).delete("record-uuid-1");

        mockMvc.perform(delete("/public/terminology-store/record-uuid-1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /public/terminology-store/{id}: returns 404 Not Found when missing")
    void delete_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("Record not found with ID: missing-id"))
                .when(service).delete("missing-id");

        mockMvc.perform(delete("/public/terminology-store/missing-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }
}

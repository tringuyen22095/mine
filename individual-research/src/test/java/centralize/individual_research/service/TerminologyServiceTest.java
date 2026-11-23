package centralize.individual_research.service;

import centralize.individual_research.dto.TerminologyRequest;
import centralize.individual_research.dto.TerminologyResponse;
import centralize.individual_research.exception.ResourceNotFoundException;
import centralize.individual_research.model.TerminologyRecord;
import centralize.individual_research.repository.TerminologyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TerminologyServiceTest {

    @Mock
    private TerminologyRepository repository;

    @InjectMocks
    private TerminologyService service;

    private TerminologyRecord sampleRecord;

    @BeforeEach
    void setUp() {
        sampleRecord = TerminologyRecord.builder()
                .id("123e4567-e89b-12d3-a456-426614174000")
                .category("Cloud")
                .keywords(List.of("firebase", "firestore"))
                .description("Cloud Firestore document database")
                .build();
    }

    @Test
    @DisplayName("create: generates UUID, trims and dedupes keywords, and saves record")
    void create_success() {
        TerminologyRequest request = TerminologyRequest.builder()
                .category("  Cloud  ")
                .keywords(List.of("  firebase  ", "firestore", "firebase", ""))
                .description("  Cloud Firestore document database  ")
                .build();

        when(repository.save(any(TerminologyRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TerminologyResponse response = service.create(request);

        assertThat(response.getId()).isNotBlank();
        assertThat(response.getCategory()).isEqualTo("Cloud");
        assertThat(response.getKeywords()).containsExactly("firebase", "firestore");
        assertThat(response.getDescription()).isEqualTo("Cloud Firestore document database");

        verify(repository).save(any(TerminologyRecord.class));
    }

    @Test
    @DisplayName("getById: returns record when found")
    void getById_found() {
        when(repository.findById("123e4567-e89b-12d3-a456-426614174000")).thenReturn(Optional.of(sampleRecord));

        TerminologyResponse response = service.getById("123e4567-e89b-12d3-a456-426614174000");

        assertThat(response.getId()).isEqualTo(sampleRecord.getId());
        assertThat(response.getCategory()).isEqualTo("Cloud");
        assertThat(response.getKeywords()).containsExactly("firebase", "firestore");
        assertThat(response.getDescription()).isEqualTo("Cloud Firestore document database");
    }

    @Test
    @DisplayName("getById: throws ResourceNotFoundException when record not found")
    void getById_notFound() {
        when(repository.findById("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById("non-existent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Record not found with ID: non-existent");
    }

    @Test
    @DisplayName("getById: throws IllegalArgumentException when id is blank")
    void getById_blankId() {
        assertThatThrownBy(() -> service.getById("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID must not be blank");
    }

    @Test
    @DisplayName("getAll: returns all records mapped to responses")
    void getAll_success() {
        when(repository.findAll()).thenReturn(List.of(sampleRecord));

        List<TerminologyResponse> responses = service.getAll();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().getId()).isEqualTo(sampleRecord.getId());
    }

    @Test
    @DisplayName("searchByKeyword: returns matching records")
    void searchByKeyword_success() {
        when(repository.findByKeyword("firestore")).thenReturn(List.of(sampleRecord));

        List<TerminologyResponse> responses = service.searchByKeyword("firestore");

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().getId()).isEqualTo(sampleRecord.getId());
    }

    @Test
    @DisplayName("searchByKeyword: throws IllegalArgumentException when keyword is blank")
    void searchByKeyword_blankKeyword() {
        assertThatThrownBy(() -> service.searchByKeyword("  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Search keyword must not be blank");
    }

    @Test
    @DisplayName("update: updates existing record successfully")
    void update_success() {
        when(repository.existsById("123e4567-e89b-12d3-a456-426614174000")).thenReturn(true);
        when(repository.save(any(TerminologyRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TerminologyRequest request = TerminologyRequest.builder()
                .category("Database")
                .keywords(List.of("nosql", "firebase"))
                .description("Updated description")
                .build();

        TerminologyResponse response = service.update("123e4567-e89b-12d3-a456-426614174000", request);

        assertThat(response.getId()).isEqualTo("123e4567-e89b-12d3-a456-426614174000");
        assertThat(response.getCategory()).isEqualTo("Database");
        assertThat(response.getKeywords()).containsExactly("nosql", "firebase");
        assertThat(response.getDescription()).isEqualTo("Updated description");
    }

    @Test
    @DisplayName("update: throws ResourceNotFoundException when record not found")
    void update_notFound() {
        when(repository.existsById("non-existent")).thenReturn(false);

        TerminologyRequest request = TerminologyRequest.builder()
                .category("Database")
                .keywords(List.of("nosql"))
                .description("Desc")
                .build();

        assertThatThrownBy(() -> service.update("non-existent", request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Record not found with ID: non-existent");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("delete: deletes record when found")
    void delete_success() {
        when(repository.deleteById("123e4567-e89b-12d3-a456-426614174000")).thenReturn(true);

        service.delete("123e4567-e89b-12d3-a456-426614174000");

        verify(repository).deleteById("123e4567-e89b-12d3-a456-426614174000");
    }

    @Test
    @DisplayName("delete: throws ResourceNotFoundException when record not found")
    void delete_notFound() {
        when(repository.deleteById("non-existent")).thenReturn(false);

        assertThatThrownBy(() -> service.delete("non-existent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Record not found with ID: non-existent");
    }
}

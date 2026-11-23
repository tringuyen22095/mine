package centralize.individual_research.service;

import centralize.individual_research.dto.TerminologyRequest;
import centralize.individual_research.dto.TerminologyResponse;
import centralize.individual_research.exception.ResourceNotFoundException;
import centralize.individual_research.model.TerminologyRecord;
import centralize.individual_research.repository.TerminologyRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class TerminologyService {

    private final TerminologyRepository repository;

    public TerminologyService(TerminologyRepository repository) {
        this.repository = repository;
    }

    public TerminologyResponse create(TerminologyRequest request) {
        List<String> cleanedKeywords = cleanKeywords(request.getKeywords());

        TerminologyRecord record = TerminologyRecord.builder()
                .id(UUID.randomUUID().toString())
                .category(request.getCategory().trim())
                .keywords(cleanedKeywords)
                .description(request.getDescription().trim())
                .build();

        TerminologyRecord saved = repository.save(record);
        return toResponse(saved);
    }

    public TerminologyResponse getById(String id) {
        if (!StringUtils.hasText(id)) {
            throw new IllegalArgumentException("ID must not be blank");
        }
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found with ID: " + id));
    }

    public List<TerminologyResponse> getAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<TerminologyResponse> searchByKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw new IllegalArgumentException("Search keyword must not be blank");
        }
        return repository.findByKeyword(keyword.trim()).stream()
                .map(this::toResponse)
                .toList();
    }

    public TerminologyResponse update(String id, TerminologyRequest request) {
        if (!StringUtils.hasText(id)) {
            throw new IllegalArgumentException("ID must not be blank");
        }
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Record not found with ID: " + id);
        }

        List<String> cleanedKeywords = cleanKeywords(request.getKeywords());

        TerminologyRecord updated = TerminologyRecord.builder()
                .id(id)
                .category(request.getCategory().trim())
                .keywords(cleanedKeywords)
                .description(request.getDescription().trim())
                .build();

        TerminologyRecord saved = repository.save(updated);
        return toResponse(saved);
    }

    public void delete(String id) {
        if (!StringUtils.hasText(id)) {
            throw new IllegalArgumentException("ID must not be blank");
        }
        boolean deleted = repository.deleteById(id);
        if (!deleted) {
            throw new ResourceNotFoundException("Record not found with ID: " + id);
        }
    }

    private List<String> cleanKeywords(List<String> keywords) {
        if (keywords == null) {
            return List.of();
        }
        return keywords.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
    }

    private TerminologyResponse toResponse(TerminologyRecord record) {
        return TerminologyResponse.builder()
                .id(record.getId())
                .category(record.getCategory())
                .keywords(record.getKeywords())
                .description(record.getDescription())
                .build();
    }
}

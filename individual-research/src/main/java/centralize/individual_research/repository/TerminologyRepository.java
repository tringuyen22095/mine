package centralize.individual_research.repository;

import centralize.individual_research.model.TerminologyRecord;

import java.util.List;
import java.util.Optional;

public interface TerminologyRepository {
    TerminologyRecord save(TerminologyRecord record);
    Optional<TerminologyRecord> findById(String id);
    List<TerminologyRecord> findAll();
    List<TerminologyRecord> findByKeyword(String keyword);
    boolean existsById(String id);
    boolean deleteById(String id);
}

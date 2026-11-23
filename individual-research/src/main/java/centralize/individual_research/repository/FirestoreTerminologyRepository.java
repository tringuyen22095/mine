package centralize.individual_research.repository;

import centralize.individual_research.config.FirebaseProperties;
import centralize.individual_research.model.TerminologyRecord;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Repository
public class FirestoreTerminologyRepository implements TerminologyRepository {

    private final Firestore firestore;
    private final FirebaseProperties properties;

    public FirestoreTerminologyRepository(Firestore firestore, FirebaseProperties properties) {
        this.firestore = firestore;
        this.properties = properties;
    }

    private CollectionReference getCollection() {
        return firestore.collection(properties.getCollectionName());
    }

    @Override
    public TerminologyRecord save(TerminologyRecord record) {
        if (!StringUtils.hasText(record.getId())) {
            record.setId(UUID.randomUUID().toString());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", record.getId());
        data.put("category", record.getCategory());
        data.put("keywords", record.getKeywords() != null ? record.getKeywords() : Collections.emptyList());
        data.put("description", record.getDescription());

        try {
            getCollection().document(record.getId()).set(data).get();
            return record;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while saving record to Firestore", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to save record to Firestore: " + e.getCause().getMessage(), e);
        }
    }

    @Override
    public Optional<TerminologyRecord> findById(String id) {
        try {
            DocumentSnapshot snapshot = getCollection().document(id).get().get();
            if (snapshot.exists()) {
                return Optional.of(mapDocument(snapshot));
            }
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while reading record from Firestore", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to read record from Firestore: " + e.getCause().getMessage(), e);
        }
    }

    @Override
    public List<TerminologyRecord> findAll() {
        try {
            ApiFuture<QuerySnapshot> future = getCollection().get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<TerminologyRecord> records = new ArrayList<>();
            for (QueryDocumentSnapshot doc : documents) {
                records.add(mapDocument(doc));
            }
            return records;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while querying records from Firestore", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to query records from Firestore: " + e.getCause().getMessage(), e);
        }
    }

    @Override
    public List<TerminologyRecord> findByKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }
        String normalizedKeyword = keyword.trim().toLowerCase();
        List<TerminologyRecord> all = findAll();
        return all.stream()
                .filter(record -> record.getKeywords() != null && record.getKeywords().stream()
                        .anyMatch(k -> k != null && k.toLowerCase().contains(normalizedKeyword)))
                .toList();
    }

    @Override
    public boolean existsById(String id) {
        try {
            DocumentSnapshot snapshot = getCollection().document(id).get().get();
            return snapshot.exists();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while checking record existence in Firestore", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to check record existence in Firestore: " + e.getCause().getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        if (!existsById(id)) {
            return false;
        }
        try {
            getCollection().document(id).delete().get();
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while deleting record from Firestore", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to delete record from Firestore: " + e.getCause().getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private TerminologyRecord mapDocument(DocumentSnapshot doc) {
        List<String> keywords = (List<String>) doc.get("keywords");
        return TerminologyRecord.builder()
                .id(doc.getString("id") != null ? doc.getString("id") : doc.getId())
                .category(doc.getString("category"))
                .keywords(keywords != null ? keywords : Collections.emptyList())
                .description(doc.getString("description"))
                .build();
    }
}

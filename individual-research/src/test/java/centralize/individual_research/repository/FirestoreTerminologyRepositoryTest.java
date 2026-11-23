package centralize.individual_research.repository;

import centralize.individual_research.config.FirebaseProperties;
import centralize.individual_research.model.TerminologyRecord;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FirestoreTerminologyRepositoryTest {

    @Mock
    private Firestore firestore;

    @Mock
    private CollectionReference collectionReference;

    @Mock
    private DocumentReference documentReference;

    @Mock
    private ApiFuture<WriteResult> writeFuture;

    @Mock
    private ApiFuture<DocumentSnapshot> docFuture;

    @Mock
    private ApiFuture<QuerySnapshot> queryFuture;

    @Mock
    private DocumentSnapshot documentSnapshot;

    @Mock
    private QuerySnapshot querySnapshot;

    @Mock
    private QueryDocumentSnapshot queryDocSnapshot;

    private FirebaseProperties properties;
    private FirestoreTerminologyRepository repository;

    @BeforeEach
    void setUp() {
        properties = new FirebaseProperties();
        properties.setCollectionName("terminology_store");
        when(firestore.collection("terminology_store")).thenReturn(collectionReference);
        repository = new FirestoreTerminologyRepository(firestore, properties);
    }

    @Test
    @DisplayName("save: saves record and returns record with UUID if id was null")
    void save_success() throws Exception {
        TerminologyRecord record = TerminologyRecord.builder()
                .category("Cloud")
                .keywords(List.of("firestore", "nosql"))
                .description("Cloud Firestore document store")
                .build();

        when(collectionReference.document(anyString())).thenReturn(documentReference);
        when(documentReference.set(anyMap())).thenReturn(writeFuture);
        when(writeFuture.get()).thenReturn(null);

        TerminologyRecord saved = repository.save(record);

        assertThat(saved.getId()).isNotBlank();
        assertThat(saved.getCategory()).isEqualTo("Cloud");
        verify(documentReference).set(anyMap());
    }

    @Test
    @DisplayName("findById: returns TerminologyRecord when document exists")
    void findById_exists() throws Exception {
        when(collectionReference.document("test-id")).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(docFuture);
        when(docFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getString("id")).thenReturn("test-id");
        when(documentSnapshot.getString("category")).thenReturn("Cloud");
        when(documentSnapshot.get("keywords")).thenReturn(List.of("firestore"));
        when(documentSnapshot.getString("description")).thenReturn("Store description");

        Optional<TerminologyRecord> result = repository.findById("test-id");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("test-id");
        assertThat(result.get().getCategory()).isEqualTo("Cloud");
        assertThat(result.get().getKeywords()).containsExactly("firestore");
    }

    @Test
    @DisplayName("findById: returns empty Optional when document does not exist")
    void findById_notExists() throws Exception {
        when(collectionReference.document("missing-id")).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(docFuture);
        when(docFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(false);

        Optional<TerminologyRecord> result = repository.findById("missing-id");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByKeyword: performs case-insensitive search over keywords")
    void findByKeyword_caseInsensitive() throws Exception {
        when(collectionReference.get()).thenReturn(queryFuture);
        when(queryFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(List.of(queryDocSnapshot));

        when(queryDocSnapshot.getString("id")).thenReturn("rec-1");
        when(queryDocSnapshot.getString("category")).thenReturn("Language");
        when(queryDocSnapshot.get("keywords")).thenReturn(List.of("JavaScript", "TypeScript"));
        when(queryDocSnapshot.getString("description")).thenReturn("Web languages");

        List<TerminologyRecord> matchingUpper = repository.findByKeyword("SCRIPT");
        assertThat(matchingUpper).hasSize(1);
        assertThat(matchingUpper.getFirst().getId()).isEqualTo("rec-1");

        List<TerminologyRecord> matchingLower = repository.findByKeyword("javascript");
        assertThat(matchingLower).hasSize(1);

        List<TerminologyRecord> matchingNone = repository.findByKeyword("python");
        assertThat(matchingNone).isEmpty();

        List<TerminologyRecord> blankKeyword = repository.findByKeyword("   ");
        assertThat(blankKeyword).isEmpty();
    }

    @Test
    @DisplayName("deleteById: deletes document when present and returns true")
    void deleteById_exists() throws Exception {
        when(collectionReference.document("del-id")).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(docFuture);
        when(docFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentReference.delete()).thenReturn(writeFuture);
        when(writeFuture.get()).thenReturn(null);

        boolean deleted = repository.deleteById("del-id");

        assertThat(deleted).isTrue();
        verify(documentReference).delete();
    }

    @Test
    @DisplayName("deleteById: returns false when document does not exist")
    void deleteById_notExists() throws Exception {
        when(collectionReference.document("missing-id")).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(docFuture);
        when(docFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(false);

        boolean deleted = repository.deleteById("missing-id");

        assertThat(deleted).isFalse();
    }
}

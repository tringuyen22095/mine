package centralize.individual_research.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TerminologyRecord {
    private String id;
    private String category;
    @Builder.Default
    private List<String> keywords = new ArrayList<>();
    private String description;
}

package centralize.individual_research.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TerminologyResponse {
    private String id;
    private String category;
    private List<String> keywords;
    private String description;
}

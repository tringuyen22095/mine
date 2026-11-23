package centralize.individual_research.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TerminologyRequest {
    @NotBlank(message = "Category is required and must not be blank")
    private String category;

    @NotEmpty(message = "Keywords list is required and must not be empty")
    private List<@NotBlank(message = "Keyword item must not be blank") String> keywords;

    @NotBlank(message = "Description is required and must not be blank")
    private String description;
}

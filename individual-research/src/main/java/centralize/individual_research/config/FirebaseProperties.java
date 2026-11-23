package centralize.individual_research.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "firebase")
public class FirebaseProperties {
    private boolean enabled = true;
    private String credentialsPath;
    private String projectId = "terminology-store";
    private String collectionName = "terminology_store";
}

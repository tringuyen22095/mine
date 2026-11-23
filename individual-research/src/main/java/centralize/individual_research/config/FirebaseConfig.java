package centralize.individual_research.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {
    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Bean
    @ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = true)
    public Firestore firestore(FirebaseProperties properties) throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirestoreClient.getFirestore();
        }

        GoogleCredentials credentials = null;
        if (StringUtils.hasText(properties.getCredentialsPath())) {
            File credFile = new File(properties.getCredentialsPath());
            if (credFile.exists()) {
                try (InputStream stream = new FileInputStream(credFile)) {
                    credentials = GoogleCredentials.fromStream(stream);
                    log.info("Initialized Firebase credentials from file: {}", properties.getCredentialsPath());
                }
            } else {
                InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(properties.getCredentialsPath());
                if (resourceStream != null) {
                    try (resourceStream) {
                        credentials = GoogleCredentials.fromStream(resourceStream);
                        log.info("Initialized Firebase credentials from classpath resource: {}", properties.getCredentialsPath());
                    }
                }
            }
        }

        if (credentials == null && StringUtils.hasText(System.getenv("GOOGLE_APPLICATION_CREDENTIALS"))) {
            try {
                credentials = GoogleCredentials.getApplicationDefault();
                log.info("Initialized Firebase credentials from GOOGLE_APPLICATION_CREDENTIALS environment variable");
            } catch (IOException e) {
                log.warn("Failed to read application default credentials: {}", e.getMessage());
            }
        }

        if (credentials != null) {
            FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder().setCredentials(credentials);
            if (StringUtils.hasText(properties.getProjectId())) {
                optionsBuilder.setProjectId(properties.getProjectId());
            }
            FirebaseApp.initializeApp(optionsBuilder.build());
            return FirestoreClient.getFirestore();
        }

        // When no credentials are provided (e.g. local dev, unit tests), initialize Firestore client with projectId
        log.warn("No Firebase credentials provided. Falling back to local FirestoreOptions with projectId: {}", properties.getProjectId());
        FirestoreOptions firestoreOptions = FirestoreOptions.newBuilder()
                .setProjectId(properties.getProjectId())
                .build();
        return firestoreOptions.getService();
    }
}

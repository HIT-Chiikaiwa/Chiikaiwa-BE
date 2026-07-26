package org.hit.chiikaiwabe.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Configuration
public class FirebaseConfig {
    @Value("${firebase.credentials:}")
    private String firebaseCredentials;

    @Value("${firebase.config-path:}")
    private String firebaseConfigPath;

    @PostConstruct
    public void initialize(){
        try {
            java.io.InputStream serviceAccount = null;
            if (firebaseCredentials != null && !firebaseCredentials.trim().isEmpty()) {
                serviceAccount = new java.io.ByteArrayInputStream(firebaseCredentials.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            } else if (firebaseConfigPath != null && !firebaseConfigPath.trim().isEmpty()) {
                serviceAccount = new FileInputStream(firebaseConfigPath);
            }

            if (serviceAccount == null) {
                log.warn("Firebase credentials not provided. Push notifications will fail.");
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            if(FirebaseApp.getApps().isEmpty()){
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            log.error("Firebase initialize fail", e);
        }
    }

}

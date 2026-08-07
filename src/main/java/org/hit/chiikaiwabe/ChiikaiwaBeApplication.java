package org.hit.chiikaiwabe;

import org.hit.chiikaiwabe.config.properties.AdminInfoProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties({AdminInfoProperties.class})
@SpringBootApplication
public class ChiikaiwaBeApplication {

    @PostConstruct
    public void init() {
        // init method can be used for other purposes
    }

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(ChiikaiwaBeApplication.class, args);
    }

}

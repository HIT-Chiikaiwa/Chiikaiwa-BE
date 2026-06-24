package org.hit.chiikaiwabe.domain.entity.common;

import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.config.properties.AdminInfoProperties;
import org.hit.chiikaiwabe.domain.entity.Role;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final AdminInfoProperties adminInfoProperties;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByUsername(adminInfoProperties.getUsername()).isEmpty()) {
            User adminUser = User.builder()
                    .username(adminInfoProperties.getUsername())
                    .password(passwordEncoder.encode(adminInfoProperties.getPassword()))
                    .firstName(adminInfoProperties.getFirstName())
                    .lastName(adminInfoProperties.getLastName())
                    .role(Role.ROLE_ADMIN)
                    .gender("OTHER")
                    .age(0)
                    .location("N/A")
                    .trustScore(100.0)
                    .status("ACTIVE")
                    .build();
            userRepository.save(adminUser);
        }
    }
}

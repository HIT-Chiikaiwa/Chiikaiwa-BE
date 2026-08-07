package org.hit.chiikaiwabe.domain.entity.common;

import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.config.properties.AdminInfoProperties;
import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.enums.Role;
import org.hit.chiikaiwabe.domain.enums.UserStatus;
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



        if(userRepository.findByUsername(adminInfoProperties.getUsername()).isEmpty()){
            User adminUser = User.builder()
                    .username(adminInfoProperties.getUsername())
                    .password(passwordEncoder.encode(adminInfoProperties.getPassword()))
                    .firstName(adminInfoProperties.getFirstName())
                    .lastName(adminInfoProperties.getLastName())
                    .dateOfBirth(java.time.LocalDate.of(2000, 1, 1))
                    .gender("MALE")
                    .userstatus(UserStatus.ACTIVE)
                    .trustScore(100.0)
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(adminUser);
        }


    }
}

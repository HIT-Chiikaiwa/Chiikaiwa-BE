package org.hit.chiikaiwabe.domain.entity.common;

import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.config.properties.AdminInfoProperties;
import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.constant.RoleConstant;
import org.hit.chiikaiwabe.domain.entity.Role;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.repository.RoleRepository;
import org.hit.chiikaiwabe.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AdminInfoProperties adminInfoProperties;

    @Override
    public void run(String... args) throws Exception {
        if(roleRepository.count() == 0){
            roleRepository.save(Role.builder().name(RoleConstant.ADMIN).build());
            roleRepository.save(Role.builder().name(RoleConstant.USER).build());
        }


        if(userRepository.findByUsername(adminInfoProperties.getUsername()).isEmpty()){
            Role adminRole = roleRepository.findByRoleName(RoleConstant.ADMIN);
            if(adminRole == null){
                throw new RuntimeException(ErrorMessage.Admin.ERR_NOT_FiND_NAME);
            }
            User adminUser = User.builder()
                    .username(adminInfoProperties.getUsername())
                    .password(adminInfoProperties.getPassword())
                    .firstName(adminInfoProperties.getFirstName())
                    .lastName(adminInfoProperties.getLastName())
                    .role(adminRole).build();
            userRepository.save(adminUser);
        }


    }
}

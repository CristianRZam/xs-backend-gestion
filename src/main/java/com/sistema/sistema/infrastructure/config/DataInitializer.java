/*package com.sistema.sistema.infrastructure.config;


import com.sistema.sistema.domain.model.User;
import com.sistema.sistema.domain.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner init(UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {
            User u = User.builder()
                    .username("admin")
                    .password(encoder.encode("1234"))
                    .roles(Set.of("ROLE_ADMIN"))
                    .build();
            userRepository.save(u);
        };
    }
}
*/
package it.atrevisan.staffmanagement.security;

import it.atrevisan.staffmanagement.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class SecurityConfig {

    @Bean
    CommandLineRunner init(UserService repo) {
        return args -> {

            if (!repo.userExists("admin")) {

                Set<String> roles = new HashSet<>();
                roles.add("ROLE_ADMIN");

                repo.saveIfNotExist("admin", "admin123", roles);
            }
        };
    }

}
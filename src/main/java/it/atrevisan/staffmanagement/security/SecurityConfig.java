package it.atrevisan.staffmanagement.security;

import it.atrevisan.staffmanagement.enums.Roles;
import it.atrevisan.staffmanagement.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class SecurityConfig {

    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    @Bean
    CommandLineRunner init(UserService repo) {
        return args -> {

            if (!repo.userExists(DEFAULT_ADMIN_USERNAME)) {

                Set<String> roles = new HashSet<>();
                roles.add(String.valueOf(Roles.ADMIN));

                repo.createIfNotExist(DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD, roles);
            }
        };
    }

}
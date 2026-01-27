package com.example.rolebase.config;

import com.example.rolebase.dto.request.AdminRegistrationRequest;
import com.example.rolebase.entity.Role;
import com.example.rolebase.repository.RoleRepository;
import com.example.rolebase.repository.UserRepository;
import com.example.rolebase.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile(value = "test")
public class DatabaseInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing database...");

        createRoleIfNotExists("USER", "Standard application user");
        createRoleIfNotExists("ADMIN", "System Administrator");
        createRoleIfNotExists("MANAGER", "Application Manager");

        try {
            createUserIfNotExists("admin", "Admin@123", "admin@example.com", Set.of("ADMIN"));
            createUserIfNotExists("manager", "Manager@123", "manager@example.com", Set.of("MANAGER"));
            createUserIfNotExists("user", "User@123", "user@example.com", Set.of("USER"));
            log.info("✓ Test users initialized");
        } catch (DataAccessException e) {
            log.error("Database error during initialization: {}",
                    e.getMostSpecificCause().getMessage());
        }
        log.info("Database initialization completed!");
    }

    private void createRoleIfNotExists(String name, String description) {
        if (roleRepository.findByName(name).isEmpty()) {
            Role role = new Role();
            role.setName(name);
            role.setDescription(description);
            roleRepository.save(role);
            log.info("  Created role: {}", name);
        }
    }

    private void createUserIfNotExists(String username, String password, String email, Set<String> roles) {
        if (userRepository.findByUsernameIgnoreCase(username).isEmpty()) {

            AdminRegistrationRequest request = AdminRegistrationRequest.builder()
                    .username(username)
                    .email(email)
                    .password(password)
                    .roles(roles).build();

            userService.registerUserByAdmin(request);
            log.info("  Created user: {} with roles {}", username, roles);
        }
    }
}
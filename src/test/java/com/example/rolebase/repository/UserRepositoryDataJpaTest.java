package com.example.rolebase.repository;

import com.example.rolebase.entity.Role;
import com.example.rolebase.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryDataJpaTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findByUsernameWithRoles_returnsUserAndRoleAssociations() {
        Role userRole = new Role();
        userRole.setName("USER");
        userRole.setDescription("Standard user role");
        roleRepository.save(userRole);

        User user = new User();
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPassword("encoded");
        user.addRole(userRole);
        userRepository.saveAndFlush(user);

        Optional<User> found = userRepository.findByUsernameWithRoles("John");

        assertTrue(found.isPresent());
        assertEquals("john", found.get().getUsername());
        assertEquals(1, found.get().getRoles().size());
        assertEquals("USER", found.get().getRoles().iterator().next().getRole().getName());
    }

    @Test
    void updateUserEnabledStatus_updatesMatchingUserCaseInsensitively() {
        User user = new User();
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPassword("encoded");
        user.setEnabled(true);
        User saved = userRepository.saveAndFlush(user);

        int updatedRows = userRepository.updateUserEnabledStatus("JOHN", false);
        userRepository.flush();
        entityManager.clear();

        User reloaded = userRepository.findById(saved.getId()).orElseThrow();
        assertEquals(1, updatedRows);
        assertFalse(reloaded.isEnabled());
    }

    @Test
    void updateUserEnabledStatus_returnsZeroWhenUserMissing() {
        int updatedRows = userRepository.updateUserEnabledStatus("missing-user", false);

        assertEquals(0, updatedRows);
    }
}

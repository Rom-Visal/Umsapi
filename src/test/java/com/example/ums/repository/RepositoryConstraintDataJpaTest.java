package com.example.ums.repository;

import com.example.ums.entity.RefreshToken;
import com.example.ums.entity.Role;
import com.example.ums.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@ActiveProfiles("test")
class RepositoryConstraintDataJpaTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void saveUser_throwsOnDuplicateEmail() {
        User first = new User();
        first.setUsername("john");
        first.setEmail("same@example.com");
        first.setPassword("encoded");
        userRepository.saveAndFlush(first);

        User second = new User();
        second.setUsername("john2");
        second.setEmail("same@example.com");
        second.setPassword("encoded");

        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(second));
    }

    @Test
    void saveRole_throwsOnDuplicateName() {
        Role first = new Role();
        first.setName("USER");
        roleRepository.saveAndFlush(first);

        Role second = new Role();
        second.setName("USER");

        assertThrows(DataIntegrityViolationException.class, () -> roleRepository.saveAndFlush(second));
    }

    @Test
    void saveToken_duplicateHash_throws() {
        User user = new User();
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPassword("encoded");
        User savedUser = userRepository.saveAndFlush(user);

        RefreshToken first = new RefreshToken();
        first.setUser(savedUser);
        first.setTokenHash("same-hash");
        first.setExpiresAt(LocalDateTime.now().plusDays(7));
        first.setRevoked(false);
        refreshTokenRepository.saveAndFlush(first);

        RefreshToken second = new RefreshToken();
        second.setUser(savedUser);
        second.setTokenHash("same-hash");
        second.setExpiresAt(LocalDateTime.now().plusDays(7));
        second.setRevoked(false);

        assertThrows(DataIntegrityViolationException.class,
                () -> refreshTokenRepository.saveAndFlush(second));
    }
}

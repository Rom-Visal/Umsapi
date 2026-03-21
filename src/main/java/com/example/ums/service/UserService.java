package com.example.ums.service;

import com.example.ums.dto.request.AdminRegistrationRequest;
import com.example.ums.dto.request.RegistrationRequest;
import com.example.ums.dto.request.UpdateUserRequest;
import com.example.ums.dto.response.UpdateUserResponse;
import com.example.ums.dto.response.UserResponse;
import com.example.ums.entity.Role;
import com.example.ums.entity.User;
import com.example.ums.exception.UserNotFoundException;
import com.example.ums.mapper.AdminRegistrationMapper;
import com.example.ums.mapper.UpdateUserRequestMapper;
import com.example.ums.mapper.UserMapper;
import com.example.ums.repository.RoleRepository;
import com.example.ums.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    private final UserMapper userMapper;
    private final AdminRegistrationMapper adminMapper;
    private final UpdateUserRequestMapper updateMapper;

    /**
     * Registers user; encodes password; persists and returns a result
     */
    public UserResponse registerUser(RegistrationRequest request) {
        validateUsernameAndEmail(request.getUsername(), request.getEmail());
        log.info("Proceeding with registration for username: {}", request.getUsername());

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.addRole(resolveDefaultRole());
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    /**
     * Registers user with roles; persists and returns a result
     */
    public UserResponse registerUserByAdmin(AdminRegistrationRequest request) {
        validateUsernameAndEmail(request.getUsername(), request.getEmail());

        User user = adminMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        resolveRoles(request.getRoles()).forEach(user::addRole);

        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    /**
     * Updates user details, including password if provided
     */
    public UpdateUserResponse updateUser(String currentUsername, UpdateUserRequest updatedDetails) {
        User existingUser = userRepository.findByUsernameWithRoles(currentUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!existingUser.isEnabled()) {
            throw new DisabledException(
                    "Your account has been disabled! You cannot edit the information.");
        }

        UserResponse beforeUpdate = userMapper.toResponse(existingUser);
        updateMapper.updateUserFromRequest(updatedDetails, existingUser);

        // Updates password if provided in request
        if (updatedDetails.getPassword() != null && !updatedDetails.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(updatedDetails.getPassword()));
        }

        User savedUser = userRepository.save(existingUser);
        return UpdateUserResponse.builder()
                .beforeUpdate(beforeUpdate)
                .afterUpdate(userMapper.toResponse(savedUser))
                .build();
    }

    public void updateUserStatus(String username, boolean isEnabled) {
        int updatedRows = userRepository.updateUserEnabledStatus(username, isEnabled);

        if (updatedRows == 0) {
            throw new UserNotFoundException("User not found");
        }

        log.info("User {} enabled status updated to {}", username, isEnabled);
    }

    public Page<UserResponse> getAll(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(userMapper::toResponse);
    }

    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID " + id));
        return userMapper.toResponse(user);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found");
        }
        userRepository.deleteById(userId);
        log.info("User deleted with ID: {}", userId);
    }

    public UserResponse getProfile(String username) {
        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException("Authentication user not found in database: " + username));
        return userMapper.toResponse(user);
    }

    // Helper method
    private void validateUsernameAndEmail(String username, String email) {
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }
    }

    /**
     * Resolves role names to roles; defaults if empty
     */
    private Set<Role> resolveRoles(Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return Set.of(resolveDefaultRole());
        }

        Set<Role> foundRoles = roleRepository.findAllByNameIn(roleNames);
        // Verifies all requested roles exist; throws if not
        if (foundRoles.size() != roleNames.size()) {
            Set<String> foundNames = foundRoles.stream().map(Role::getName)
                    .collect(Collectors.toSet());

            List<String> invalidRoles = roleNames.stream()
                    .filter(name -> !foundNames.contains(name))
                    .toList();
            throw new IllegalArgumentException("The following roles do not exist: " + invalidRoles);
        }
        return foundRoles;
    }

    private Role resolveDefaultRole() {
        return roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Default USER role not found"));
    }
}
package com.example.rolebase.service;

import com.example.rolebase.dto.request.AdminRegistrationRequest;
import com.example.rolebase.dto.request.RegistrationRequest;
import com.example.rolebase.dto.request.UpdateUserRequest;
import com.example.rolebase.dto.response.UpdateUserResponse;
import com.example.rolebase.dto.response.UserResponse;
import com.example.rolebase.entity.Role;
import com.example.rolebase.entity.User;
import com.example.rolebase.exception.UserNotFoundException;
import com.example.rolebase.mapper.AdminRegistrationMapper;
import com.example.rolebase.mapper.UpdateUserRequestMapper;
import com.example.rolebase.mapper.UserMapper;
import com.example.rolebase.repository.RoleRepository;
import com.example.rolebase.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
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

    public UserResponse registerUser(RegistrationRequest request) {
        validateUsernameAndEmail(request.getUsername(), request.getEmail());
        log.info("Proceeding with registration for username: {}", request.getUsername());

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role defaultRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Default USER role not found in the system."));

        user.addRole(defaultRole);
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    public UserResponse registerUserByAdmin(AdminRegistrationRequest request) {
        validateUsernameAndEmail(request.getUsername(), request.getEmail());

        User user = adminMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        Set<Role> rolesToAssign = new HashSet<>();

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            List<Role> foundRoles = roleRepository.findAllByNameIn(request.getRoles());

            Set<String> foundRoleNames = foundRoles.stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());
            List<String> invalidRoles = request.getRoles().stream()
                    .filter(name -> !foundRoleNames.contains(name))
                    .toList();

            if (!invalidRoles.isEmpty()) {
                throw new IllegalArgumentException("The following roles do not exist: " + invalidRoles);
            }
            rolesToAssign.addAll(foundRoles);
        } else {
            Role defaultRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new IllegalStateException("Default USER role not found"));
            rolesToAssign.add(defaultRole);
        }

        rolesToAssign.forEach(user::addRole);
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    public UpdateUserResponse updateUser(String currentUsername, UpdateUserRequest updatedDetails) {
        User existingUser = userRepository.findByUsernameWithRolesIgnoreCase(currentUsername)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!existingUser.isEnabled()) {
            throw new DisabledException(
                    "Your account has been disabled! You cannot edit the information.");
        }

        UserResponse beforeUpdate = userMapper.toResponse(existingUser);
        updateMapper.updateUserFromRequest(updatedDetails, existingUser);

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
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException("User nod found"));

        user.setEnabled(isEnabled);
        userRepository.save(user);
        log.info("User {} enabled status updated to {}, username, isEnabled", username, isEnabled);
    }

    public List<UserResponse> getAll() {
        List<User> user = userRepository.findAll();
        return userMapper.toResponseList(user);
    }

    public UserResponse getUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID " + id));
        return userMapper.toResponse(user);
    }

    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found");
        }
        userRepository.deleteById(userId);
        log.info("User deleted with ID: {}", userId);
    }

    public UserResponse getProfile(String username) {
        User user = userRepository.findByUsernameWithRolesIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Authentication user not found in database: " + username));
        return userMapper.toResponse(user);
    }

    // Helper method
    private void validateUsernameAndEmail(String username, String email) {
        if (userRepository.findByUsernameIgnoreCase(username).isPresent()) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email is already registered");
        }
    }
}
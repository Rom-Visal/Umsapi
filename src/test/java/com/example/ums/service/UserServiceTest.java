package com.example.ums.service;

import com.example.ums.dto.request.AdminRegistrationRequest;
import com.example.ums.dto.request.RegistrationRequest;
import com.example.ums.dto.request.UpdateUserRequest;
import com.example.ums.dto.response.UserResponse;
import com.example.ums.entity.Role;
import com.example.ums.entity.User;
import com.example.ums.exception.UserNotFoundException;
import com.example.ums.mapper.AdminRegistrationMapper;
import com.example.ums.mapper.UpdateUserRequestMapper;
import com.example.ums.mapper.UserMapper;
import com.example.ums.repository.RoleRepository;
import com.example.ums.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AdminRegistrationMapper adminMapper;

    @Mock
    private UpdateUserRequestMapper updateMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUser_success() {
        RegistrationRequest request = RegistrationRequest.builder()
                .username("john")
                .email("john@example.com")
                .password("Password@123")
                .build();

        Role userRole = new Role();
        userRole.setName("USER");

        User mappedUser = new User();
        mappedUser.setUsername("john");
        mappedUser.setEmail("john@example.com");

        UserResponse expectedResponse = UserResponse.builder()
                .id(1L)
                .username("john")
                .email("john@example.com")
                .roles(Set.of("USER"))
                .enabled(true)
                .build();

        when(userRepository.existsByUsernameIgnoreCase("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(mappedUser);
        when(passwordEncoder.encode("Password@123")).thenReturn("encoded");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(userMapper.toResponse(mappedUser)).thenReturn(expectedResponse);

        UserResponse response = userService.registerUser(request);

        assertSame(expectedResponse, response);
        assertEquals("encoded", mappedUser.getPassword());
        assertNotNull(mappedUser.getRoles());
        assertEquals(1, mappedUser.getRoles().size());

        verify(userRepository).save(mappedUser);
    }

    @Test
    void registerUser_usernameExists() {
        RegistrationRequest request = RegistrationRequest.builder()
                .username("john")
                .email("john@example.com")
                .password("Password@123")
                .build();

        when(userRepository.existsByUsernameIgnoreCase("john")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(request));

        assertEquals("Username is already taken", exception.getMessage());
    }

    @Test
    void updateUser_disabledUser() {
        User existingUser = new User();
        existingUser.setEnabled(false);

        when(userRepository.findByUsernameWithRoles("john")).thenReturn(Optional.of(existingUser));

        assertThrows(DisabledException.class,
                () -> userService.updateUser("john", UpdateUserRequest.builder().build()));
    }

    @Test
    void updateUserStatus_notFound() {
        when(userRepository.updateUserEnabledStatus("john", true)).thenReturn(0);

        assertThrows(UserNotFoundException.class, () -> userService.updateUserStatus("john", true));
    }

    @Test
    void updateUserStatus_updatesEnabledFlag() {
        when(userRepository.updateUserEnabledStatus("john", true)).thenReturn(1);

        userService.updateUserStatus("john", true);

        verify(userRepository).updateUserEnabledStatus("john", true);
    }

    @Test
    void deleteUser_notFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(99L));
    }

    @Test
    void getProfile_notFound() {
        when(userRepository.findByUsernameWithRoles("john")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.getProfile("john"));
    }

    @Test
    void updateUser_encodesNewPasswordWhenProvided() {
        User existingUser = new User();
        existingUser.setEnabled(true);

        UpdateUserRequest request = UpdateUserRequest.builder()
                .password("NewPassword@123")
                .build();

        UserResponse beforeUpdate = UserResponse.builder().username("john").build();
        UserResponse afterUpdate = UserResponse.builder().username("john").build();

        when(userRepository.findByUsernameWithRoles("john")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("NewPassword@123")).thenReturn("encoded-new-password");
        when(userMapper.toResponse(existingUser)).thenReturn(beforeUpdate, afterUpdate);
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        userService.updateUser("john", request);

        verify(updateMapper).updateUserFromRequest(request, existingUser);
        assertEquals("encoded-new-password", existingUser.getPassword());
        verify(userRepository).save(existingUser);
        verify(userMapper, times(2)).toResponse(existingUser);
    }

    @Test
    void updateUser_notFound() {
        when(userRepository.findByUsernameWithRoles("john")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.updateUser("john", UpdateUserRequest.builder().build()));
    }

    @Test
    void registerUser_roleMissing() {
        RegistrationRequest request = RegistrationRequest.builder()
                .username("john")
                .email("john@example.com")
                .password("Password@123")
                .build();

        when(userRepository.existsByUsernameIgnoreCase("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(new User());
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> userService.registerUser(request));
    }

    @Test
    void registerUser_emailExists() {
        RegistrationRequest request = RegistrationRequest.builder()
                .username("john")
                .email("john@example.com")
                .password("Password@123")
                .build();

        when(userRepository.existsByUsernameIgnoreCase("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(request));
        assertEquals("Email is already registered", exception.getMessage());
    }

    @Test
    void registerByAdmin_invalidRole() {
        AdminRegistrationRequest request = AdminRegistrationRequest.builder()
                .username("john")
                .email("john@example.com")
                .password("Password@123")
                .roles(Set.of("USER", "MISSING"))
                .build();

        Role userRole = new Role();
        userRole.setName("USER");

        when(userRepository.existsByUsernameIgnoreCase("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(adminMapper.toEntity(request)).thenReturn(new User());
        when(passwordEncoder.encode("Password@123")).thenReturn("encoded");
        when(roleRepository.findAllByNameIn(request.getRoles())).thenReturn(Set.of(userRole));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.registerUserByAdmin(request));
        assertTrue(exception.getMessage().contains("The following roles do not exist"));
    }

    @Test
    void registerByAdmin_defaultRole() {
        AdminRegistrationRequest request = AdminRegistrationRequest.builder()
                .username("john")
                .email("john@example.com")
                .password("Password@123")
                .roles(null)
                .build();

        Role defaultRole = new Role();
        defaultRole.setName("USER");
        User mappedUser = new User();
        UserResponse expected = UserResponse.builder().username("john").build();

        when(userRepository.existsByUsernameIgnoreCase("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(adminMapper.toEntity(request)).thenReturn(mappedUser);
        when(passwordEncoder.encode("Password@123")).thenReturn("encoded");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(defaultRole));
        when(userMapper.toResponse(mappedUser)).thenReturn(expected);

        UserResponse actual = userService.registerUserByAdmin(request);

        assertEquals(expected, actual);
        assertEquals(1, mappedUser.getRoles().size());
        verify(userRepository).save(mappedUser);
    }
}

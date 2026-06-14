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
import org.junit.jupiter.api.BeforeEach;
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

    private static final String TEST_USERNAME = "john";
    private static final String TEST_EMAIL = "john@example.com";
    private static final String TEST_PASSWORD = "Password@123";
    private static final String TEST_NEW_PASSWORD = "NewPassword@123";
    private static final String TEST_ENCODED_PASSWORD = "encoded";
    private static final String TEST_ENCODED_NEW_PASSWORD = "encoded-new-password";
    private static final String ROLE_USER = "USER";
    private static final String ROLE_MISSING = "MISSING";
    private static final Long TEST_USER_ID = 1L;
    private static final Long UNKNOWN_USER_ID = 99L;

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

    private RegistrationRequest registrationRequest;

    @BeforeEach
    void setUp() {
        registrationRequest = createRegistrationRequest();
    }

    @Test
    void registerUser_success() {
        // Arrange
        Role userRole = createRole();
        User mappedUser = createUser();
        UserResponse expectedResponse = createUserResponse(TEST_USER_ID, Set.of(ROLE_USER));

        when(userRepository.existsByUsernameIgnoreCase(TEST_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(userMapper.toEntity(registrationRequest)).thenReturn(mappedUser);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(TEST_ENCODED_PASSWORD);
        when(roleRepository.findByName(ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userMapper.toResponse(mappedUser)).thenReturn(expectedResponse);

        // Act
        UserResponse response = userService.registerUser(registrationRequest);

        // Assert
        assertSame(expectedResponse, response);
        verify(passwordEncoder).encode(TEST_PASSWORD);
        verify(userRepository).save(argThat(this::hasExpectedUserFieldsAndSingleRole));
    }

    @Test
    void registerUser_usernameExists() {
        when(userRepository.existsByUsernameIgnoreCase(TEST_USERNAME)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(registrationRequest));

        assertEquals("Username is already taken", exception.getMessage());
    }

    @Test
    void updateUser_disabledUser() {
        User existingUser = createUser();
        existingUser.setEnabled(false);

        when(userRepository.findByUsernameWithRoles(TEST_USERNAME)).thenReturn(Optional.of(existingUser));

        assertThrows(DisabledException.class,
                () -> userService.updateUser(TEST_USERNAME, UpdateUserRequest.builder().build()));
    }

    @Test
    void updateUserStatus_notFound() {
        when(userRepository.updateUserEnabledStatus(TEST_USERNAME, true)).thenReturn(0);

        assertThrows(UserNotFoundException.class, () -> userService.updateUserStatus(TEST_USERNAME, true));
    }

    @Test
    void updateUserStatus_updatesEnabledFlag() {
        when(userRepository.updateUserEnabledStatus(TEST_USERNAME, true)).thenReturn(1);

        userService.updateUserStatus(TEST_USERNAME, true);

        verify(userRepository).updateUserEnabledStatus(TEST_USERNAME, true);
    }

    @Test
    void deleteUser_notFound() {
        when(userRepository.existsById(UNKNOWN_USER_ID)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(UNKNOWN_USER_ID));
    }

    @Test
    void getProfile_notFound() {
        when(userRepository.findByUsernameWithRoles(TEST_USERNAME)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.getProfile(TEST_USERNAME));
    }

    @Test
    void updateUser_encodesNewPasswordWhenProvided() {
        // Arrange
        User existingUser = createUser();
        UpdateUserRequest request = UpdateUserRequest.builder()
                .password(TEST_NEW_PASSWORD)
                .build();
        UserResponse beforeUpdate = createUserResponse(null, Set.of(ROLE_USER));
        UserResponse afterUpdate = createUserResponse(null, Set.of(ROLE_USER));

        when(userRepository.findByUsernameWithRoles(TEST_USERNAME)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(TEST_NEW_PASSWORD)).thenReturn(TEST_ENCODED_NEW_PASSWORD);
        when(userMapper.toResponse(existingUser)).thenReturn(beforeUpdate, afterUpdate);
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        // Act
        userService.updateUser(TEST_USERNAME, request);

        // Assert
        verify(updateMapper).updateUserFromRequest(request, existingUser);
        verify(passwordEncoder).encode(TEST_NEW_PASSWORD);
        verify(userRepository).save(argThat(user -> TEST_ENCODED_NEW_PASSWORD.equals(user.getPassword())));
        verify(userMapper, times(2)).toResponse(existingUser);
    }

    @Test
    void updateUser_notFound() {
        when(userRepository.findByUsernameWithRoles(TEST_USERNAME)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(TEST_USERNAME, UpdateUserRequest.builder().build()));
    }

    @Test
    void registerUser_roleMissing() {
        when(userRepository.existsByUsernameIgnoreCase(TEST_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(userMapper.toEntity(registrationRequest)).thenReturn(new User());
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(roleRepository.findByName(ROLE_USER)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> userService.registerUser(registrationRequest));
    }

    @Test
    void registerUser_emailExists() {
        when(userRepository.existsByUsernameIgnoreCase(TEST_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(registrationRequest));
        assertEquals("Email is already registered", exception.getMessage());
    }

    @Test
    void registerByAdmin_invalidRole() {
        // Arrange
        AdminRegistrationRequest request = createAdminRegistrationRequest(Set.of(ROLE_USER, ROLE_MISSING));
        Role userRole = createRole();

        when(userRepository.existsByUsernameIgnoreCase(TEST_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(adminMapper.toEntity(request)).thenReturn(createUser());
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(TEST_ENCODED_PASSWORD);
        when(roleRepository.findAllByNameIn(request.getRoles())).thenReturn(Set.of(userRole));

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.registerUserByAdmin(request));

        // Assert
        assertTrue(exception.getMessage().contains("The following roles do not exist"));
        assertTrue(exception.getMessage().contains(ROLE_MISSING));
    }

    @Test
    void registerByAdmin_defaultRole() {
        // Arrange
        AdminRegistrationRequest request = createAdminRegistrationRequest(null);
        Role defaultRole = createRole();
        User mappedUser = createUser();
        UserResponse expected = createUserResponse(null, Set.of(ROLE_USER));

        when(userRepository.existsByUsernameIgnoreCase(TEST_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(adminMapper.toEntity(request)).thenReturn(mappedUser);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(TEST_ENCODED_PASSWORD);
        when(roleRepository.findByName(ROLE_USER)).thenReturn(Optional.of(defaultRole));
        when(userMapper.toResponse(mappedUser)).thenReturn(expected);

        // Act
        UserResponse actual = userService.registerUserByAdmin(request);

        // Assert
        assertEquals(expected, actual);
        verify(userRepository).save(argThat(this::hasExpectedUserFieldsAndSingleRole));
    }

    private RegistrationRequest createRegistrationRequest() {
        return RegistrationRequest.builder()
                .username(TEST_USERNAME)
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();
    }

    private AdminRegistrationRequest createAdminRegistrationRequest(Set<String> roles) {
        return AdminRegistrationRequest.builder()
                .username(TEST_USERNAME)
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .roles(roles)
                .build();
    }

    private User createUser() {
        User user = new User();
        user.setUsername(TEST_USERNAME);
        user.setEmail(TEST_EMAIL);
        user.setEnabled(true);
        return user;
    }

    private Role createRole() {
        Role role = new Role();
        role.setName(UserServiceTest.ROLE_USER);
        return role;
    }

    private UserResponse createUserResponse(Long id, Set<String> roles) {
        return UserResponse.builder()
                .id(id)
                .username(TEST_USERNAME)
                .email(TEST_EMAIL)
                .roles(roles)
                .enabled(true)
                .build();
    }

    private boolean hasExpectedUserFieldsAndSingleRole(User user) {
        return TEST_USERNAME.equals(user.getUsername())
                && TEST_EMAIL.equals(user.getEmail())
                && UserServiceTest.TEST_ENCODED_PASSWORD.equals(user.getPassword())
                && user.getRoles() != null
                && user.getRoles().size() == 1
                && user.getRoles().stream().anyMatch(role -> UserServiceTest.ROLE_USER.equals(role.getRole().getName()));
    }
}

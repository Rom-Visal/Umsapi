package com.example.ums.controller;

import com.example.ums.dto.response.AuthResponse;
import com.example.ums.dto.response.UserResponse;
import com.example.ums.security.JwtUtils;
import com.example.ums.service.AuthService;
import com.example.ums.service.UserDetailsServiceImpl;
import com.example.ums.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerWebMvcTest {

    private static final String HOME_URL = "/auth/home";
    private static final String REGISTER_URL = "/auth/register";
    private static final String LOGIN_URL = "/auth/login";
    private static final String LOGOUT_URL = "/auth/logout";

    private static final String VALID_USERNAME = "john";
    private static final String VALID_EMAIL = "john@example.com";
    private static final String VALID_PASSWORD = "Password@123";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        when(userService.registerUser(any())).thenReturn(buildUserResponse());
        when(authService.login(any())).thenReturn(buildAuthResponse());
        doNothing().when(authService).logout(any());
    }

    @Test
    void home_returnsWelcomeMessage() throws Exception {
        mockMvc.perform(get(HOME_URL))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome to Spring Boot REST APIs"));
    }

    @Test
    void register_returnsCreatedUser() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value(VALID_USERNAME))
                .andExpect(jsonPath("$.email").value(VALID_EMAIL))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void login_returnsTokenPayload() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(VALID_USERNAME, VALID_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessTokenExpiresIn").value(900000))
                .andExpect(jsonPath("$.refreshTokenExpiresIn").value(604800000));
    }

    @Test
    void register_invalidPayload_returnsBadRequest() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "",
                                  "email": "bad-email",
                                  "password": "short"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    void logout_returnsNoContent() throws Exception {
        mockMvc.perform(post(LOGOUT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "some-refresh-token"
                                }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void login_invalidCredentials_returnsUnauthorized() throws Exception {
        when(authService.login(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "wrong-pass"
                                }
                                """.formatted(VALID_USERNAME)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication required. Please provide valid credentials."))
                .andExpect(jsonPath("$.path").value(LOGIN_URL));
    }

    private static UserResponse buildUserResponse() {
        return UserResponse.builder()
                .id(1L)
                .username(VALID_USERNAME)
                .email(VALID_EMAIL)
                .roles(Set.of("USER"))
                .enabled(true)
                .build();
    }

    private static AuthResponse buildAuthResponse() {
        return AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .accessTokenExpiresIn(900000L)
                .refreshTokenExpiresIn(604800000L)
                .build();
    }
}

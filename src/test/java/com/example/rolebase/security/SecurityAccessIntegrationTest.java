package com.example.rolebase.security;

import com.example.rolebase.dto.response.UserResponse;
import com.example.rolebase.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void publicAuthEndpoint_isAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/auth/home"))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome to Spring Boot REST APIs"));
    }

    @Test
    void protectedManagerEndpoint_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/manager/user/all"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedUserEndpoint_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/user/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "plain-user", roles = "USER")
    void managerEndpoint_forbiddenForUserRole() throws Exception {
        mockMvc.perform(get("/manager/user/all"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void managerEndpoint_allowsManagerRole() throws Exception {
        Page<UserResponse> users = new PageImpl<>(List.of(
                UserResponse.builder()
                        .id(10L)
                        .username("alpha")
                        .email("alpha@example.com")
                        .roles(Set.of("USER"))
                        .enabled(true)
                        .build()
        ));
        when(userService.getAll(any(Pageable.class))).thenReturn(users);

        mockMvc.perform(get("/manager/user/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("alpha"));

        verify(userService).getAll(any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void managerEndpoint_allowsAdminRole() throws Exception {
        when(userService.getAll(any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/manager/user/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void adminEndpoint_forbiddenForNonAdminRole() throws Exception {
        mockMvc.perform(delete("/admin/delete-user/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminEndpoint_allowsAdminRole() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/admin/delete-user/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("User with ID 1 deleted successfully."));

        verify(userService).deleteUser(1L);
    }

    @Test
    @WithMockUser(username = "Alice", roles = "USER")
    void userProfileEndpoint_allowsUserRole() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(1L)
                .username("Alice")
                .email("alice@example.com")
                .roles(Set.of("USER"))
                .enabled(true)
                .build();
        when(userService.getProfile("Alice")).thenReturn(response);

        mockMvc.perform(get("/user/profile").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));

        verify(userService).getProfile("Alice");
    }
}

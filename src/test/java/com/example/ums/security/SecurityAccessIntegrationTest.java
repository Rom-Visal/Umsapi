package com.example.ums.security;

import com.example.ums.dto.response.UserResponse;
import com.example.ums.service.UserService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAccessIntegrationTest {

    private static final String PUBLIC_HOME = "/auth/home";
    private static final String MANAGER_USERS = "/manager/user/all";
    private static final String USER_PROFILE = "/user/profile";
    private static final String ADMIN_DELETE_USER = "/admin/delete-user/1";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void publicEndpoint_returnsWelcomeMessage() throws Exception {
        mockMvc.perform(get(PUBLIC_HOME))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome to Spring Boot REST APIs"));
    }

    @Test
    void managerEndpoint_returnsUnauthorizedWhenAnonymous() throws Exception {
        mockMvc.perform(get(MANAGER_USERS))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userEndpoint_returnsUnauthorizedWhenAnonymous() throws Exception {
        mockMvc.perform(get(USER_PROFILE))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminDelete_returnsUnauthorizedWhenAnonymous() throws Exception {
        mockMvc.perform(delete(ADMIN_DELETE_USER))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "plain-user", roles = "USER")
    void managerEndpoint_returnsForbiddenForUserRole() throws Exception {
        mockMvc.perform(get(MANAGER_USERS))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void managerEndpoint_returnsUsersForManagerRole() throws Exception {
        Page<UserResponse> users = new PageImpl<>(List.of(sampleUserResponse(10L, "alpha", "alpha@example.com")));
        when(userService.getAll(any(Pageable.class))).thenReturn(users);

        mockMvc.perform(get(MANAGER_USERS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10L))
                .andExpect(jsonPath("$.content[0].username").value("alpha"))
                .andExpect(jsonPath("$.content[0].email").value("alpha@example.com"))
                .andExpect(jsonPath("$.content[0].roles[0]").value("USER"))
                .andExpect(jsonPath("$.content[0].enabled").value(true));

        verify(userService).getAll(any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void managerEndpoint_returnsOkForAdminRole() throws Exception {
        when(userService.getAll(any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get(MANAGER_USERS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0));

        verify(userService).getAll(any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void adminEndpoint_returnsForbiddenForManagerRole() throws Exception {
        mockMvc.perform(delete(ADMIN_DELETE_USER))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminEndpoint_deletesUserForAdminRole() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete(ADMIN_DELETE_USER))
                .andExpect(status().isOk())
                .andExpect(content().string("User with ID 1 deleted successfully."));

        verify(userService).deleteUser(1L);
    }

    @Test
    @WithMockUser(username = "Alice", roles = "USER")
    void userProfile_returnsProfileForUserRole() throws Exception {
        UserResponse response = sampleUserResponse(1L, "Alice", "alice@example.com");
        when(userService.getProfile("Alice")).thenReturn(response);

        mockMvc.perform(get(USER_PROFILE).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.enabled").value(true));

        verify(userService).getProfile("Alice");
    }

    private static UserResponse sampleUserResponse(Long id, String username, String email) {
        return UserResponse.builder()
                .id(id)
                .username(username)
                .email(email)
                .roles(Set.of("USER"))
                .enabled(true)
                .build();
    }
}

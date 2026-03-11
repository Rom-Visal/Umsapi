package com.example.rolebase.controller;

import com.example.rolebase.exception.UserNotFoundException;
import com.example.rolebase.security.JwtUtils;
import com.example.rolebase.service.UserDetailsServiceImpl;
import com.example.rolebase.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ManagerController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ManagerControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void getUser_returnsNotFoundContractWhenServiceThrowsUserNotFound() throws Exception {
        when(userService.getUser(99L))
                .thenThrow(new UserNotFoundException("User not found with ID 99"));

        mockMvc.perform(get("/manager/user/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found with ID 99"))
                .andExpect(jsonPath("$.path").value("/manager/user/99"));
    }
}

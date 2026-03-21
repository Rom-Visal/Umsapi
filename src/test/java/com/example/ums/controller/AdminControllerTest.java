package com.example.ums.controller;

import com.example.ums.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class AdminControllerTest {

    @Test
    void ping_returnsSuccessMessage() {
        UserService userService = mock(UserService.class);
        AdminController controller = new AdminController(userService);

        ResponseEntity<String> response = controller.ping();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Admin endpoint is working.", response.getBody());
    }
}

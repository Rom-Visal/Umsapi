package com.example.ums.exception;

import com.example.ums.dto.response.ErrorResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    @Test
    void handleGlobalException_devProfile_detailedMsg() {
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        GlobalExceptionHandler handler = new GlobalExceptionHandler(environment);
        WebRequest request = new ServletWebRequest(new MockHttpServletRequest("GET", "/any/path"));

        ResponseEntity<ErrorResponse> response = handler.handleGlobalException(
                new RuntimeException("boom"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("/any/path", response.getBody().getPath());
        assertTrue(response.getBody().getMessage().contains("An unexpected error occurred:"));
    }

    @Test
    void handleGlobalException_otherProfile_genericMsg() {
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        GlobalExceptionHandler handler = new GlobalExceptionHandler(environment);
        WebRequest request = new ServletWebRequest(new MockHttpServletRequest("GET", "/any/path"));

        ResponseEntity<ErrorResponse> response = handler.handleGlobalException(
                new RuntimeException("boom"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("An unexpected error occurred. Please try again later", response.getBody().getMessage());
    }
}

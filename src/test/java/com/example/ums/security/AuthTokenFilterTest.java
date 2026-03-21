package com.example.ums.security;

import com.example.ums.service.UserDetailsServiceImpl;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthTokenFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private AuthTokenFilter authTokenFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_setsAuthenticationWhenAccessTokenIsValid() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("john")
                .password("encoded")
                .authorities("ROLE_USER")
                .build();

        when(jwtUtils.validateAccessToken("valid-token")).thenReturn(true);
        when(jwtUtils.getUsernameFromAccessToken("valid-token")).thenReturn("john");
        when(userDetailsService.loadUserByUsername("john")).thenReturn(userDetails);

        authTokenFilter.doFilter(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("john", SecurityContextHolder.getContext().getAuthentication().getName());
        assertNotNull(filterChain.getRequest());
        verify(userDetailsService).loadUserByUsername("john");
    }

    @Test
    void doFilterInternal_doesNotAuthenticateWhenTokenInvalid() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(jwtUtils.validateAccessToken("invalid-token")).thenReturn(false);

        authTokenFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(filterChain.getRequest());
    }

    @Test
    void doFilterInternal_skipsJwtValidationWhenAuthorizationHeaderMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        authTokenFilter.doFilter(request, response, filterChain);

        verifyNoInteractions(jwtUtils, userDetailsService);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(filterChain.getRequest());
    }

    @Test
    void doFilterInternal_skipsJwtValidationWhenAuthorizationHeaderIsNotBearer() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        authTokenFilter.doFilter(request, response, filterChain);

        verifyNoInteractions(jwtUtils, userDetailsService);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(filterChain.getRequest());
    }

    @Test
    void doFilterInternal_continuesChainWhenJwtProcessingThrowsException() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer boom-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(jwtUtils.validateAccessToken("boom-token"))
                .thenThrow(new RuntimeException("JWT parse error"));

        authTokenFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(response.getStatus() == 200 || response.getStatus() == 0);
        assertNotNull(filterChain.getRequest());
    }
}

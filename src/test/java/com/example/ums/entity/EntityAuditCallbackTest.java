package com.example.ums.entity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EntityAuditCallbackTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void userRoleOnCreate_setsSystemWhenNoAuthentication() {
        UserRole userRole = new UserRole();

        userRole.onCreate();

        assertNotNull(userRole.getAssignAt());
        assertEquals("SYSTEM", userRole.getAssignBy());
    }

    @Test
    void userRoleOnCreate_setsSelfRegisteredForAnonymousUser() {
        UserRole userRole = new UserRole();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymousUser", null, List.of())
        );

        userRole.onCreate();

        assertEquals("SELF_REGISTERED", userRole.getAssignBy());
    }

    @Test
    void userRoleOnCreate_setsAuthenticatedUsername() {
        UserRole userRole = new UserRole();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("john", null, List.of())
        );

        userRole.onCreate();

        assertEquals("john", userRole.getAssignBy());
    }

    @Test
    void refreshTokenOnCreate_setsTimestampWhenMissing() {
        RefreshToken token = new RefreshToken();

        token.onCreate();

        assertNotNull(token.getCreatedAt());
    }

    @Test
    void refreshTokenOnCreate_keepsExistingTimestamp() {
        RefreshToken token = new RefreshToken();
        LocalDateTime fixed = LocalDateTime.now().minusDays(1);
        token.setCreatedAt(fixed);

        token.onCreate();

        assertEquals(fixed, token.getCreatedAt());
    }
}

package com.example.rolebase.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private static final String ACCESS_SECRET = "VGhpc0lzQVNlY3VyZUJhc2U2NFNlY3JldEtleUZvckpXVFRva2VuMTIzNDU2Nzg5MA==";
    private static final String REFRESH_SECRET = "U2Vjb25kU2VjdXJlQmFzZTY0UmVmcmVzaEtleUZvclNwcmluZ0Jvb3QxMjM0NTY3ODkw";

    @Test
    void accessAndRefreshTokens_haveDistinctTypesAndValidationRules() {
        JwtUtils jwtUtils = configuredJwtUtils(ACCESS_SECRET, ACCESS_SECRET, 900000L);
        UserDetails user = User.withUsername("john").password("encoded").authorities("USER").build();

        String accessToken = jwtUtils.generateAccessToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user);

        assertTrue(jwtUtils.validateAccessToken(accessToken));
        assertTrue(jwtUtils.validateRefreshToken(refreshToken));
        assertFalse(jwtUtils.validateAccessToken(refreshToken));
        assertFalse(jwtUtils.validateRefreshToken(accessToken));
        assertNotEquals(accessToken, refreshToken);
    }

    @Test
    void expiredAccessToken_throwsExpiredJwtExceptionOnValidation() {
        JwtUtils jwtUtils = configuredJwtUtils(ACCESS_SECRET, REFRESH_SECRET, -1000L);
        UserDetails user = User.withUsername("john").password("encoded").authorities("USER").build();
        String accessToken = jwtUtils.generateAccessToken(user);

        assertThrows(ExpiredJwtException.class, () -> jwtUtils.validateAccessToken(accessToken));
    }

    @Test
    void wrongSecret_rejectsToken() {
        JwtUtils issuer = configuredJwtUtils(ACCESS_SECRET, REFRESH_SECRET, 900000L);
        JwtUtils validator = configuredJwtUtils(REFRESH_SECRET, REFRESH_SECRET, 900000L);
        UserDetails user = User.withUsername("john").password("encoded").authorities("USER").build();

        String accessToken = issuer.generateAccessToken(user);

        assertFalse(validator.validateAccessToken(accessToken));
    }

    @Test
    void refreshTokenCarriesExpectedSubjectAndExpiration() {
        JwtUtils jwtUtils = configuredJwtUtils(ACCESS_SECRET, REFRESH_SECRET, 900000L);
        UserDetails user = User.withUsername("john").password("encoded").authorities("USER").build();

        String refreshToken = jwtUtils.generateRefreshToken(user);

        assertTrue(jwtUtils.validateRefreshToken(refreshToken));
        assertNotNull(jwtUtils.getRefreshTokenExpiration(refreshToken));
        assertNotNull(jwtUtils.getUsernameFromRefreshToken(refreshToken));
    }

    private JwtUtils configuredJwtUtils(String accessSecret, String refreshSecret,
                                        long accessExpirationMs) {
        JwtUtils jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "accessSecret", accessSecret);
        ReflectionTestUtils.setField(jwtUtils, "refreshSecret", refreshSecret);
        ReflectionTestUtils.setField(jwtUtils, "accessTokenExpirationMs", accessExpirationMs);
        ReflectionTestUtils.setField(jwtUtils, "refreshTokenExpirationMs", 604800000L);
        return jwtUtils;
    }
}

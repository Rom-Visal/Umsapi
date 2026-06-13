package com.example.ums.service;

import com.example.ums.dto.request.LoginRequest;
import com.example.ums.dto.request.RefreshTokenRequest;
import com.example.ums.dto.response.AuthResponse;
import com.example.ums.entity.RefreshToken;
import com.example.ums.entity.User;
import com.example.ums.repository.RefreshTokenRepository;
import com.example.ums.repository.UserRepository;
import com.example.ums.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.core.userdetails.User.withUsername;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final long ACCESS_TOKEN_EXPIRATION_MS = 900000L;
    private static final long REFRESH_TOKEN_EXPIRATION_MS = 604800000L;
    private static final Instant FIXED_EXPIRES_AT = Instant.parse("2026-03-10T10:15:30Z");
    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 3, 10, 10, 15, 30);

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_shouldReturnTokensAndPersistRefreshToken() {
        LoginRequest request = loginRequest("john", "Password@123");
        UserDetails userDetails = userDetails("john");
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        User user = user(10L, "john");

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByUsernameIgnoreCase("john")).thenReturn(Optional.of(user));
        when(jwtUtils.generateAccessToken(userDetails)).thenReturn("access-token");
        when(jwtUtils.generateRefreshToken(userDetails)).thenReturn("refresh-token");
        when(jwtUtils.getAccessTokenExpirationMs()).thenReturn(ACCESS_TOKEN_EXPIRATION_MS);
        when(jwtUtils.getRefreshTokenExpirationMs()).thenReturn(REFRESH_TOKEN_EXPIRATION_MS);
        when(jwtUtils.getRefreshTokenExpiration("refresh-token")).thenReturn(Date.from(FIXED_EXPIRES_AT));

        AuthResponse response = authService.login(request);

        assertAll(
                () -> assertEquals("access-token", response.getAccessToken()),
                () -> assertEquals("refresh-token", response.getRefreshToken()),
                () -> assertEquals("Bearer", response.getTokenType()),
                () -> assertEquals(ACCESS_TOKEN_EXPIRATION_MS, response.getAccessTokenExpiresIn()),
                () -> assertEquals(REFRESH_TOKEN_EXPIRATION_MS, response.getRefreshTokenExpiresIn())
        );

        verify(authenticationManager).authenticate(any());
        verify(userRepository).findByUsernameIgnoreCase("john");
        verify(refreshTokenRepository).revokeAllActiveTokensByUserId(eq(10L), any());

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());

        RefreshToken savedToken = tokenCaptor.getValue();
        assertAll(
                () -> assertEquals(user, savedToken.getUser()),
                () -> assertFalse(savedToken.isRevoked()),
                () -> assertNotNull(savedToken.getExpiresAt())
        );
    }

    @Test
    void refreshToken_shouldRejectInvalidJwt() {
        RefreshTokenRequest request = refreshTokenRequest("invalid");
        when(jwtUtils.validateRefreshToken("invalid")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.refreshToken(request));
    }

    @Test
    void logout_shouldRevokeStoredToken() throws Exception {
        String rawToken = "refresh-token";
        String tokenHash = hash(rawToken);
        RefreshTokenRequest request = refreshTokenRequest(rawToken);
        RefreshToken token = new RefreshToken();
        token.setRevoked(false);

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(token));

        authService.logout(request);

        assertTrue(token.isRevoked());
        verify(refreshTokenRepository).save(token);
    }

    @Test
    void logout_shouldIgnoreMissingToken() throws Exception {
        String rawToken = "refresh-token";
        String tokenHash = hash(rawToken);
        RefreshTokenRequest request = refreshTokenRequest(rawToken);

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.empty());

        authService.logout(request);

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void refreshToken_shouldDetectReusedToken() throws Exception {
        String rawToken = "raw-token";
        String tokenHash = hash(rawToken);
        RefreshTokenRequest request = refreshTokenRequest(rawToken);
        User user = user(99L, "john");
        RefreshToken stored = refreshToken(user, true, FIXED_NOW.plusMinutes(10), tokenHash);

        when(jwtUtils.validateRefreshToken(rawToken)).thenReturn(true);
        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(stored));

        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> authService.refreshToken(request));

        assertEquals("Refresh token reuse detected, please login again", exception.getMessage());
        verify(refreshTokenRepository).revokeAllActiveTokensByUserId(eq(99L), any());
    }

    @Test
    void refreshToken_shouldRejectExpiredToken() throws Exception {
        String rawToken = "raw-token";
        String tokenHash = hash(rawToken);
        RefreshTokenRequest request = refreshTokenRequest(rawToken);
        User user = user(99L, "john");
        RefreshToken stored = refreshToken(user, false, FIXED_NOW.minusMinutes(1), tokenHash);

        when(jwtUtils.validateRefreshToken(rawToken)).thenReturn(true);
        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(stored));

        CredentialsExpiredException exception = assertThrows(CredentialsExpiredException.class,
                () -> authService.refreshToken(request));

        assertEquals("Refresh token expired, please login again", exception.getMessage());
        assertAll(
                () -> assertTrue(stored.isRevoked()),
                () -> assertNotNull(stored.getRevokedAt())
        );
        verify(refreshTokenRepository).save(stored);
    }

    @Test
    void refreshToken_shouldRejectSubjectMismatch() throws Exception {
        String rawToken = "raw-token";
        String tokenHash = hash(rawToken);
        RefreshTokenRequest request = refreshTokenRequest(rawToken);
        User user = user(99L, "john");
        RefreshToken stored = refreshToken(user, false, FIXED_NOW.plusMinutes(10), tokenHash);

        when(jwtUtils.validateRefreshToken(rawToken)).thenReturn(true);
        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(stored));
        when(jwtUtils.getUsernameFromRefreshToken(rawToken)).thenReturn("mismatch-user");

        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> authService.refreshToken(request));

        assertEquals("Invalid refresh token subject", exception.getMessage());
    }

    @Test
    void refreshToken_shouldRotateTokenSuccessfully() throws Exception {
        String rawToken = "raw-token";
        String tokenHash = hash(rawToken);
        String rotatedRefresh = "new-refresh-token";
        String rotatedRefreshHash = hash(rotatedRefresh);
        RefreshTokenRequest request = refreshTokenRequest(rawToken);
        User user = user(100L, "john");
        RefreshToken stored = refreshToken(user, false, FIXED_NOW.plusMinutes(10), tokenHash);
        UserDetails userDetails = userDetails("john");

        when(jwtUtils.validateRefreshToken(rawToken)).thenReturn(true);
        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(stored));
        when(jwtUtils.getUsernameFromRefreshToken(rawToken)).thenReturn("john");
        when(userDetailsService.loadUserByUsername("john")).thenReturn(userDetails);
        when(jwtUtils.generateAccessToken(userDetails)).thenReturn("new-access-token");
        when(jwtUtils.generateRefreshToken(userDetails)).thenReturn(rotatedRefresh);
        when(jwtUtils.getRefreshTokenExpiration(rotatedRefresh)).thenReturn(Date.from(FIXED_EXPIRES_AT));
        when(jwtUtils.getAccessTokenExpirationMs()).thenReturn(ACCESS_TOKEN_EXPIRATION_MS);
        when(jwtUtils.getRefreshTokenExpirationMs()).thenReturn(REFRESH_TOKEN_EXPIRATION_MS);

        AuthResponse response = authService.refreshToken(request);

        assertAll(
                () -> assertEquals("new-access-token", response.getAccessToken()),
                () -> assertEquals(rotatedRefresh, response.getRefreshToken())
        );

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, times(2)).save(captor.capture());
        List<RefreshToken> savedTokens = captor.getAllValues();

        RefreshToken revokedToken = savedTokens.get(0);
        RefreshToken newToken = savedTokens.get(1);

        assertAll(
                () -> assertTrue(revokedToken.isRevoked()),
                () -> assertEquals(rotatedRefreshHash, revokedToken.getReplacedByTokenHash()),
                () -> assertFalse(newToken.isRevoked()),
                () -> assertEquals(rotatedRefreshHash, newToken.getTokenHash()),
                () -> assertEquals(user, newToken.getUser())
        );
    }

    private LoginRequest loginRequest(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    private RefreshTokenRequest refreshTokenRequest(String refreshToken) {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(refreshToken);
        return request;
    }

    private User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private UserDetails userDetails(String username) {
        return withUsername(username)
                .password("encoded")
                .authorities("USER")
                .build();
    }

    private RefreshToken refreshToken(User user, boolean revoked, LocalDateTime expiresAt, String tokenHash) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setRevoked(revoked);
        token.setExpiresAt(expiresAt);
        token.setTokenHash(tokenHash);
        return token;
    }

    private String hash(String token) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }
}

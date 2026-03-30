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
    void login_success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("Password@123");

        UserDetails userDetails = withUsername("john")
                .password("encoded")
                .authorities("USER")
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        User user = new User();
        user.setId(10L);
        user.setUsername("john");

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByUsernameIgnoreCase("john")).thenReturn(Optional.of(user));
        when(jwtUtils.generateAccessToken(userDetails)).thenReturn("access-token");
        when(jwtUtils.generateRefreshToken(userDetails)).thenReturn("refresh-token");
        when(jwtUtils.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(jwtUtils.getRefreshTokenExpirationMs()).thenReturn(604800000L);
        when(jwtUtils.getRefreshTokenExpiration("refresh-token"))
                .thenReturn(Date.from(Instant.parse("2026-03-10T10:15:30Z")));

        AuthResponse response = authService.login(request);

        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(900000L, response.getAccessTokenExpiresIn());
        assertEquals(604800000L, response.getRefreshTokenExpiresIn());

        verify(refreshTokenRepository).revokeAllActiveTokensByUserId(any(), any());
        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());

        RefreshToken savedToken = tokenCaptor.getValue();
        assertEquals(user, savedToken.getUser());
        assertFalse(savedToken.isRevoked());
        assertNotNull(savedToken.getExpiresAt());
    }

    @Test
    void refreshToken_invalidJwt() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid");
        when(jwtUtils.validateRefreshToken("invalid")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.refreshToken(request));
    }

    @Test
    void logout_success() throws Exception {
        String rawToken = "refresh-token";
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(rawToken);

        RefreshToken token = new RefreshToken();
        token.setRevoked(false);

        when(refreshTokenRepository.findByTokenHash(hash(rawToken))).thenReturn(Optional.of(token));

        authService.logout(request);

        assertTrue(token.isRevoked());
        verify(refreshTokenRepository).save(token);
    }

    @Test
    void logout_notFound() throws Exception {
        String rawToken = "refresh-token";
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(rawToken);
        when(refreshTokenRepository.findByTokenHash(hash(rawToken))).thenReturn(Optional.empty());

        authService.logout(request);

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void refreshToken_revokedToken() throws Exception {
        String rawToken = "raw-token";
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(rawToken);

        User user = new User();
        user.setId(99L);
        user.setUsername("john");

        RefreshToken stored = new RefreshToken();
        stored.setUser(user);
        stored.setRevoked(true);
        stored.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        stored.setTokenHash(hash(rawToken));

        when(jwtUtils.validateRefreshToken(rawToken)).thenReturn(true);
        when(refreshTokenRepository.findByTokenHash(hash(rawToken))).thenReturn(Optional.of(stored));

        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> authService.refreshToken(request));
        assertEquals("Refresh token reuse detected, please login again", exception.getMessage());
        verify(refreshTokenRepository).revokeAllActiveTokensByUserId(eq(99L), any());
    }

    @Test
    void refreshToken_expiredToken() throws Exception {
        String rawToken = "raw-token";
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(rawToken);

        User user = new User();
        user.setId(99L);
        user.setUsername("john");

        RefreshToken stored = new RefreshToken();
        stored.setUser(user);
        stored.setRevoked(false);
        stored.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        stored.setTokenHash(hash(rawToken));

        when(jwtUtils.validateRefreshToken(rawToken)).thenReturn(true);
        when(refreshTokenRepository.findByTokenHash(hash(rawToken))).thenReturn(Optional.of(stored));

        CredentialsExpiredException exception = assertThrows(CredentialsExpiredException.class,
                () -> authService.refreshToken(request));
        assertEquals("Refresh token expired, please login again", exception.getMessage());
        assertTrue(stored.isRevoked());
        assertNotNull(stored.getRevokedAt());
        verify(refreshTokenRepository).save(stored);
    }

    @Test
    void refreshToken_subjectMismatch() throws Exception {
        String rawToken = "raw-token";
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(rawToken);

        User user = new User();
        user.setId(99L);
        user.setUsername("john");

        RefreshToken stored = new RefreshToken();
        stored.setUser(user);
        stored.setRevoked(false);
        stored.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        stored.setTokenHash(hash(rawToken));

        when(jwtUtils.validateRefreshToken(rawToken)).thenReturn(true);
        when(refreshTokenRepository.findByTokenHash(hash(rawToken))).thenReturn(Optional.of(stored));
        when(jwtUtils.getUsernameFromRefreshToken(rawToken)).thenReturn("mismatch-user");

        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> authService.refreshToken(request));
        assertEquals("Invalid refresh token subject", exception.getMessage());
    }

    @Test
    void refreshToken_success() throws Exception {
        String rawToken = "raw-token";
        String rotatedRefresh = "new-refresh-token";
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(rawToken);

        User user = new User();
        user.setId(100L);
        user.setUsername("john");

        RefreshToken stored = new RefreshToken();
        stored.setUser(user);
        stored.setRevoked(false);
        stored.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        stored.setTokenHash(hash(rawToken));

        UserDetails userDetails = withUsername("john")
                .password("encoded")
                .authorities("USER")
                .build();

        when(jwtUtils.validateRefreshToken(rawToken)).thenReturn(true);
        when(refreshTokenRepository.findByTokenHash(hash(rawToken))).thenReturn(Optional.of(stored));
        when(jwtUtils.getUsernameFromRefreshToken(rawToken)).thenReturn("john");
        when(userDetailsService.loadUserByUsername("john")).thenReturn(userDetails);
        when(jwtUtils.generateAccessToken(userDetails)).thenReturn("new-access-token");
        when(jwtUtils.generateRefreshToken(userDetails)).thenReturn(rotatedRefresh);
        when(jwtUtils.getRefreshTokenExpiration(rotatedRefresh))
                .thenReturn(Date.from(Instant.parse("2026-03-10T10:15:30Z")));
        when(jwtUtils.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(jwtUtils.getRefreshTokenExpirationMs()).thenReturn(604800000L);

        AuthResponse response = authService.refreshToken(request);

        assertEquals("new-access-token", response.getAccessToken());
        assertEquals(rotatedRefresh, response.getRefreshToken());

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, times(2)).save(captor.capture());
        List<RefreshToken> savedTokens = captor.getAllValues();

        RefreshToken revokedToken = savedTokens.getFirst();
        assertTrue(revokedToken.isRevoked());
        assertEquals(hash(rotatedRefresh), revokedToken.getReplacedByTokenHash());

        RefreshToken newToken = savedTokens.get(1);
        assertFalse(newToken.isRevoked());
        assertEquals(hash(rotatedRefresh), newToken.getTokenHash());
        assertEquals(user, newToken.getUser());
    }

    private String hash(String token) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }
}

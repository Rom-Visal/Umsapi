package com.example.rolebase.service;

import com.example.rolebase.dto.request.LoginRequest;
import com.example.rolebase.dto.request.RefreshTokenRequest;
import com.example.rolebase.dto.response.AuthResponse;
import com.example.rolebase.entity.RefreshToken;
import com.example.rolebase.entity.User;
import com.example.rolebase.repository.RefreshTokenRepository;
import com.example.rolebase.repository.UserRepository;
import com.example.rolebase.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsernameIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Authenticated user not found"));

        refreshTokenRepository.revokeAllActiveTokensByUserId(user.getId(), LocalDateTime.now());

        String accessToken = jwtUtils.generateAccessToken(userDetails);
        String refreshToken = jwtUtils.generateRefreshToken(userDetails);
        saveRefreshToken(user, refreshToken);
        return buildAuthResponse(accessToken, refreshToken);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtUtils.validateRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        String tokenHash = hashToken(refreshToken);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (storedToken.isRevoked()) {
            refreshTokenRepository.revokeAllActiveTokensByUserId(storedToken.getUser().getId(), LocalDateTime.now());
            throw new BadCredentialsException("Refresh token reuse detected, please login again");
        }

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            storedToken.setRevoked(true);
            storedToken.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(storedToken);
            throw new CredentialsExpiredException("Refresh token expired, please login again");
        }

        String usernameFromJwt = jwtUtils.getUsernameFromRefreshToken(refreshToken);
        if (!storedToken.getUser().getUsername().equalsIgnoreCase(usernameFromJwt)) {
            throw new BadCredentialsException("Invalid refresh token subject");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(usernameFromJwt);
        String newAccessToken = jwtUtils.generateAccessToken(userDetails);
        String newRefreshToken = jwtUtils.generateRefreshToken(userDetails);

        rotateToken(storedToken, newRefreshToken);
        return buildAuthResponse(newAccessToken, newRefreshToken);
    }

    public void logout(RefreshTokenRequest request) {
        String tokenHash = hashToken(request.getRefreshToken());
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            token.setRevoked(true);
            token.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(token);
        });
    }

    private void rotateToken(RefreshToken currentToken, String newRawToken) {
        String newTokenHash = hashToken(newRawToken);
        currentToken.setRevoked(true);
        currentToken.setRevokedAt(LocalDateTime.now());
        currentToken.setReplacedByTokenHash(newTokenHash);
        refreshTokenRepository.save(currentToken);
        saveRefreshToken(currentToken.getUser(), newRawToken);
    }

    private void saveRefreshToken(User user, String rawToken) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(hashToken(rawToken));
        token.setExpiresAt(LocalDateTime.ofInstant(
                jwtUtils.getRefreshTokenExpiration(rawToken).toInstant(),
                ZoneId.systemDefault()
        ));
        token.setRevoked(false);
        refreshTokenRepository.save(token);
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .accessTokenExpiresIn(jwtUtils.getAccessTokenExpirationMs())
                .refreshTokenExpiresIn(jwtUtils.getRefreshTokenExpirationMs())
                .build();
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Unable to hash token", ex);
        }
    }
}

package com.example.rolebase.controller;

import com.example.rolebase.api.AuthApi;
import com.example.rolebase.dto.request.LoginRequest;
import com.example.rolebase.dto.request.RefreshTokenRequest;
import com.example.rolebase.dto.request.RegistrationRequest;
import com.example.rolebase.dto.response.AuthResponse;
import com.example.rolebase.dto.response.UserResponse;
import com.example.rolebase.service.AuthService;
import com.example.rolebase.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController implements AuthApi {

    private final UserService userService;
    private final AuthService authService;

    @Override
    @GetMapping("/home")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("Welcome to Spring Boot REST APIs");
    }

    @Override
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(RegistrationRequest request) {
        UserResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }
}

package com.example.rolebase.controller;

import com.example.rolebase.api.AuthApi;
import com.example.rolebase.dto.request.RegistrationRequest;
import com.example.rolebase.dto.response.UserResponse;
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
}
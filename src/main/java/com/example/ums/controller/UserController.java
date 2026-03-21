package com.example.ums.controller;

import com.example.ums.api.UserApi;
import com.example.ums.dto.request.UpdateUserRequest;
import com.example.ums.dto.response.UpdateUserResponse;
import com.example.ums.dto.response.UserResponse;
import com.example.ums.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(Authentication authentication) {
        UserResponse getProfile = userService.getProfile(authentication.getName());
        return ResponseEntity.ok(getProfile);
    }

    @Override
    @PutMapping("/update-profile")
    public ResponseEntity<UpdateUserResponse> updateProfile(UpdateUserRequest request, Authentication auth) {
        UpdateUserResponse updateResponse = userService.updateUser(auth.getName(), request);
        return ResponseEntity.ok(updateResponse);
    }
}
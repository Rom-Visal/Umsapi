package com.example.rolebase.controller;

import com.example.rolebase.api.AdminApi;
import com.example.rolebase.dto.request.AdminRegistrationRequest;
import com.example.rolebase.dto.response.UserResponse;
import com.example.rolebase.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController implements AdminApi {

    private final UserService userService;

    @Override
    @PostMapping("/create-user")
    public ResponseEntity<UserResponse> createUser(AdminRegistrationRequest request) {
        UserResponse response = userService.registerUserByAdmin(request);
        return ResponseEntity.ok(response);
    }

    @Override
    @PatchMapping("/user/{username}/status")
    public ResponseEntity<String> updateUserStatus(@PathVariable String username, boolean enabled) {
        userService.updateUserStatus(username, enabled);
        String status = enabled ? "enabled" : "disabled";
        return ResponseEntity.ok("User '" + username + "' has been " + status + " successfully.");
    }

    @Override
    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User with ID " + id + " deleted successfully.");
    }
}
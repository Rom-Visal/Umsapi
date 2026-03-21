package com.example.ums.controller;

import com.example.ums.api.AdminApi;
import com.example.ums.dto.request.AdminRegistrationRequest;
import com.example.ums.dto.response.UserResponse;
import com.example.ums.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController implements AdminApi {

    private final UserService userService;

    @Override
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Admin endpoint is working.");
    }

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

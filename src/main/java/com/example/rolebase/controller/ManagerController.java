package com.example.rolebase.controller;

import com.example.rolebase.api.ManagerApi;
import com.example.rolebase.dto.response.UserResponse;
import com.example.rolebase.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/manager")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
@RequiredArgsConstructor
public class ManagerController implements ManagerApi {

    private final UserService userService;

    @Override
    @GetMapping("/user/all")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> getResponseList = userService.getAll();
        return ResponseEntity.ok(getResponseList);
    }

    @Override
    @GetMapping("/user/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getUser(id));
    }
}
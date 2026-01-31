package com.example.rolebase.controller;

import com.example.rolebase.api.ManagerApi;
import com.example.rolebase.dto.response.UserResponse;
import com.example.rolebase.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manager")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
@RequiredArgsConstructor
public class ManagerController implements ManagerApi {

    private final UserService userService;

    @Override
    @GetMapping("/user/all")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @ParameterObject @PageableDefault Pageable pageable) {
        Page<UserResponse> pagedUsers = userService.getAll(pageable);
        return ResponseEntity.ok(pagedUsers);
    }

    @Override
    @GetMapping("/user/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }
}
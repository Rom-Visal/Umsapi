package com.example.ums.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class UserResponse {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "Username", example = "User")
    private String username;

    @Schema(description = "Email", example = "User@gmail.com")
    private String email;

    @Schema(description = "Roles", example = "[\"USER\"]")
    private Set<String> roles;

    @Schema(description = "Status", example = "true")
    private boolean enabled;
}
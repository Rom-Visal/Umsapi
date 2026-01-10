package com.example.rolebase.dto.response;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    @Schema(description = "User ID", example = "1")
    private Integer id;

    @Schema(description = "Username", example = "User")
    private String username;

    @Schema(description = "Email", example = "User@gmail.com")
    private String email;

    @Schema(description = "Roles", example = "[\"USER\"]")
    private Set<String> roles;

    @Schema(description = "Status", example = "true")
    private boolean enabled;
}
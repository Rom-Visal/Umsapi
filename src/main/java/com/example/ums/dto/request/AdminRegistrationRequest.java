package com.example.ums.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class AdminRegistrationRequest {

    @Schema(description = "Username", example = "NewUser")
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 2, max = 50, message = "Username must be between 2-50 characters")
    private String username;

    @Schema(description = "Email", example = "newemail@gmail.com")
    @Email(message = "Invalid input email.")
    @NotNull(message = "Email is required")
    private String email;

    @Schema(description = "Password", example = "newPass@123")
    @NotBlank(message = "Password cannot be blank")
    private String password;

    @Schema(description = "Roles", example = "[\"USER\", \"MANAGER\"]")
    private Set<String> roles;
}
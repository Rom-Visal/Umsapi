package com.example.ums.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUserRequest {

    @Schema(description = "Username", example = "User")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$",
            message = "Username can only contain letters, numbers, underscores and hyphens")
    private String username;

    @Schema(description = "Email", example = "newemail@gmail.com")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "Password", example = "NewPass@123")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s])\\S{8,}$",
            message = "Password must contain at least one uppercase, one lowercase, one digit and one special character")
    private String password;
}

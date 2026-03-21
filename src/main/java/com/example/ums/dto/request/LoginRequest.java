package com.example.ums.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @Schema(description = "Username", example = "User")
    @NotBlank(message = "Username cannot be blank")
    private String username;

    @Schema(description = "Password", example = "User@123")
    @NotBlank(message = "Password cannot be blank")
    private String password;
}

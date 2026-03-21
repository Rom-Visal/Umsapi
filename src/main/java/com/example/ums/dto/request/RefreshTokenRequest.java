package com.example.ums.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {

    @Schema(description = "Refresh token", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Refresh token cannot be blank")
    private String refreshToken;
}

package com.example.rolebase.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

    @Schema(description = "JWT access token")
    private String accessToken;

    @Schema(description = "JWT refresh token")
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer")
    private String tokenType;

    @Schema(description = "Access token expiration time in milliseconds", example = "900000")
    private long accessTokenExpiresIn;

    @Schema(description = "Refresh token expiration time in milliseconds", example = "604800000")
    private long refreshTokenExpiresIn;
}

package com.example.rolebase.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponse {

    @Schema(description = "Timestamp", example = "2024-01-09T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "Error type", example = "Validation Error")
    private String error;

    @Schema(description = "Error message", example = "Username is already taken")
    private String message;

    @Schema(description = "Path", example = "/auth/register")
    private String path;
}
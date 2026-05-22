package com.example.ums.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Timestamp", example = "2024-01-09 10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "Error type", example = "Validation Error")
    private String error;

    @Schema(description = "Error message", example = "Username is already taken")
    private String message;

    @Schema(description = "Path", example = "/auth/register")
    private String path;
}
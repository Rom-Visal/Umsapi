package com.example.rolebase.util;

import com.example.rolebase.dto.response.ErrorResponse;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

public final class ResponseUtils {

    public static ErrorResponse createErrorResponse(
            LocalDateTime timestamp,
            String errorType,
            String message,
            WebRequest request) {

        String path = request.getDescription(false);

        if (path.startsWith("uri=")) {
            path = path.substring(4);
        }

        return new ErrorResponse(timestamp, errorType, message, path);
    }
}
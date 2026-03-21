package com.example.ums.api;

import com.example.ums.api.common.ApiUserResponse;
import com.example.ums.dto.request.UpdateUserRequest;
import com.example.ums.dto.response.UpdateUserResponse;
import com.example.ums.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "User Profile", description = "APIs for user profile management")
@SecurityRequirement(name = "bearerAuth")
public interface UserApi {

    @Operation(summary = "Get Profile", description = "Get current user profile")
    @ApiResponse(responseCode = "401", ref = "Unauthorized")
    @ApiUserResponse
    ResponseEntity<UserResponse> getProfile(@Parameter(hidden = true) Authentication authentication);

    @Operation(summary = "Update Profile", description = "Update email or password. Account must be enabled")
    @ApiUserResponse
    @ApiResponse(responseCode = "400", ref = "BadRequest")
    @ApiResponse(responseCode = "401", ref = "Unauthorized")
    ResponseEntity<UpdateUserResponse> updateProfile(@Valid @RequestBody UpdateUserRequest request, @Parameter(hidden = true) Authentication auth);
}

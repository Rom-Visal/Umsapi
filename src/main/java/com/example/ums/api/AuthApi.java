package com.example.ums.api;

import com.example.ums.dto.request.LoginRequest;
import com.example.ums.dto.request.RefreshTokenRequest;
import com.example.ums.dto.request.RegistrationRequest;
import com.example.ums.dto.response.AuthResponse;
import com.example.ums.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Authentication", description = "Public APIs for registration and home page")
public interface AuthApi {

    @Operation(summary = "Welcome Page", description = "Public endpoint, no authentication required")
    @ApiResponse(responseCode = "200", description = "Success")
    ResponseEntity<String> home();

    @Operation(summary = "Register New User", description = "Register with USER role by default")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class)
                            , mediaType = "application/json")),
            @ApiResponse(responseCode = "400", ref = "BadRequest")
    })
    ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegistrationRequest request);

    @Operation(summary = "Login", description = "Authenticate and return JWT bearer token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authenticated successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class),
                            mediaType = "application/json")),
            @ApiResponse(responseCode = "401", ref = "Unauthorized")
    })
    ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request);

    @Operation(summary = "Refresh Access Token", description = "Rotate refresh token and issue new token pair")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tokens refreshed successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class),
                            mediaType = "application/json")),
            @ApiResponse(responseCode = "401", ref = "Unauthorized")
    })
    ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request);

    @Operation(summary = "Logout", description = "Revoke current refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Logged out successfully"),
            @ApiResponse(responseCode = "400", ref = "BadRequest")
    })
    ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request);
}

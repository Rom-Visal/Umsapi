package com.example.rolebase.api;

import com.example.rolebase.config.openapi.ApiUserListResponse;
import com.example.rolebase.config.openapi.ApiUserResponse;
import com.example.rolebase.config.openapi.SecuredEndpoint;
import com.example.rolebase.config.openapi.SecuredGetById;
import com.example.rolebase.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Manager Operations", description = "APIs for viewing users")
@SecurityRequirement(name = "basicAuth")
public interface ManagerApi {

    @Operation(summary = "Get All Users", description = "Retrieve all users in system with pagination support")
    @SecuredEndpoint
    @ApiUserListResponse
    ResponseEntity<Page<UserResponse>> getAllUsers(@ParameterObject Pageable pageable);

    @Operation(summary = "Get User by ID", description = "Get user details by ID")
    @ApiUserResponse
    @SecuredGetById
    ResponseEntity<UserResponse> getUser(@Parameter(description = "User ID", example = "1") @PathVariable Integer id);
}
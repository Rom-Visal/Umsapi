package com.example.rolebase.api;

import com.example.rolebase.api.common.ApiUserListResponse;
import com.example.rolebase.api.common.ApiUserResponse;
import com.example.rolebase.api.common.SecuredEndpoint;
import com.example.rolebase.api.common.SecuredGetById;
import com.example.rolebase.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Manager Operations", description = "APIs for viewing users")
@SecurityRequirement(name = "bearerAuth")
public interface ManagerApi {

    @Operation(summary = "Get All Users", description = "Retrieve all users in system with pagination support")
    @SecuredEndpoint
    @ApiUserListResponse
    ResponseEntity<Page<UserResponse>> getAllUsers(@ParameterObject @PageableDefault Pageable pageable);

    @Operation(summary = "Get User by ID", description = "Get user details by ID")
    @ApiUserResponse
    @SecuredGetById
    ResponseEntity<UserResponse> getUser(@Parameter(description = "User ID", example = "1") @PathVariable Long id);
}

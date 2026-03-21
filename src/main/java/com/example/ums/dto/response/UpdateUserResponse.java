package com.example.ums.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateUserResponse {

    @Schema(description = "Before update")
    private UserResponse beforeUpdate;

    @Schema(description = "After update")
    private UserResponse afterUpdate;

    @Schema(description = "Message", example = "Profile updated successfully")
    @Builder.Default
    private String message = "Profile updated successfully";
}
package com.example.rolebase.api.common;

import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(responseCode = "401", ref = "Unauthorized")
@ApiResponse(responseCode = "403", ref = "Forbidden")
public @interface SecuredEndpoint {
}

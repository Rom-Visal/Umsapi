package com.example.rolebase.config;

import com.example.rolebase.dto.response.ErrorResponse;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class OpenAPIConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "basicAuth";
        Schema<?> errorSchema = ModelConverters.getInstance()
                .resolveAsResolvedSchema(new AnnotatedType(ErrorResponse.class))
                .schema;

        // Configures API metadata, security, and standard responses
        return new OpenAPI()
                .info(new Info()
                        .title("RBAC Management System API")
                        .version("1.0")
                        // Configures security and error handling components
                        .description("Role-Based Access Control API"))
                // Configures security schemes and error response schemas
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic"))
                        .addSchemas("ErrorResponse", errorSchema)
                        .addResponses("BadRequest", createErrorResponse("Bad Request", "Validation failed"))
                        .addResponses("Unauthorized", createErrorResponse("Unauthorized", "Full authentication is required"))
                        .addResponses("Forbidden", createErrorResponse("Forbidden", "Access denied"))
                        .addResponses("NotFound", createErrorResponse("Not Found", "Resource not found")));
    }

    /**
     * Creates API response with error schema and example
     */
    private ApiResponse createErrorResponse(String error, String message) {
        Map<String, Object> exampleData = new LinkedHashMap<>();
        exampleData.put("timestamp", "2024-03-20T10:00:00Z");
        exampleData.put("error", error);
        exampleData.put("message", message);
        exampleData.put("path", "/api/v1/...");

        Example example = new Example();
        example.setValue(exampleData);

        // Defines error response with schema and example
        return new ApiResponse()
                .description(error)
                .content(new Content().addMediaType("application/json",
                        new MediaType()
                                .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
                                .addExamples("default", example)));
    }
}
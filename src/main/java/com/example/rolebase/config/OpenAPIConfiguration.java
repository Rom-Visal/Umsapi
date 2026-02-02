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
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import java.time.LocalDateTime;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class OpenAPIConfiguration {

    //Configures OpenAPI definition with security and error responses
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "basicAuth";

        Schema<?> errorSchema = ModelConverters.getInstance()
                .resolveAsResolvedSchema(new AnnotatedType(ErrorResponse.class))
                .schema;

        Info apiInfo = new Info()
                .title("RBAC Management System API")
                .version("1.0")
                .description("Role-Based Access Control API");

        SecurityScheme basicAuth = new SecurityScheme()
                .name(securitySchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("basic");

        // Defines reusable security and error response components
        Components components = new Components()
                .addSecuritySchemes(securitySchemeName, basicAuth)
                .addSchemas("ErrorResponse", errorSchema)
                .addResponses("BadRequest", createErrorResponse("Bad Request", "Validation error"))
                .addResponses("Unauthorized", createErrorResponse("Unauthorized", "Authentication required. Please provide valid credentials."))
                .addResponses("Forbidden", createErrorResponse("Forbidden", "Access denied"))
                .addResponses("NotFound", createErrorResponse("Not Found", "User not found"));

        return new OpenAPI()
                .info(apiInfo)
                .components(components);
    }

    // Creates API response with error details and schema
    private ApiResponse createErrorResponse(String error, String message) {
        ErrorResponse errorSample = ErrorResponse.builder()
                .timestamp(LocalDateTime.parse("2024-03-20T10:00:00"))
                .error(error)
                .message(message)
                .path("/api/v1/...")
                .build();

        Example example = new Example().value(errorSample);
        Schema<?> schemaRef = new Schema<>().$ref("#/components/schemas/ErrorResponse");
        MediaType mediaType = new MediaType()
                .schema(schemaRef)
                .addExamples("default", example);
        Content content = new Content().addMediaType("application/json", mediaType);

        return new ApiResponse()
                .description(error)
                .content(content);
    }
}
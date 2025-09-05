package com.crediya.iam.api.config.config;


import com.crediya.iam.api.config.OpenApiConfig;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OpenApiConfigTest {

    private OpenApiConfig openApiConfig = new OpenApiConfig();

    @Test
    void openAPI_shouldCreateValidOpenAPIConfiguration() {
        OpenAPI openAPI = openApiConfig.openAPI();

        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertEquals("IAM API", openAPI.getInfo().getTitle());
        assertEquals("v1", openAPI.getInfo().getVersion());
    }

    @Test
    void openAPI_shouldHaveBearerAuthSecurityScheme() {
        OpenAPI openAPI = openApiConfig.openAPI();

        Components components = openAPI.getComponents();
        assertNotNull(components);

        SecurityScheme bearerAuth = components.getSecuritySchemes().get("bearerAuth");
        assertNotNull(bearerAuth);
        assertEquals(SecurityScheme.Type.HTTP, bearerAuth.getType());
        assertEquals("bearer", bearerAuth.getScheme());
        assertEquals("JWT", bearerAuth.getBearerFormat());
        assertEquals("bearerAuth", bearerAuth.getName());
    }

    @Test
    void openAPI_shouldHaveGlobalSecurityRequirement() {
        OpenAPI openAPI = openApiConfig.openAPI();

        assertNotNull(openAPI.getSecurity());
        assertFalse(openAPI.getSecurity().isEmpty());
        assertTrue(openAPI.getSecurity().get(0).containsKey("bearerAuth"));
    }

    @Test
    void openAPI_methodShouldHaveBeanAnnotation() throws NoSuchMethodException {
        assertTrue(OpenApiConfig.class.getMethod("openAPI")
                .isAnnotationPresent(org.springframework.context.annotation.Bean.class));
    }

    @Test
    void class_shouldHaveConfigurationAnnotation() {
        assertTrue(OpenApiConfig.class.isAnnotationPresent(org.springframework.context.annotation.Configuration.class));
    }
}

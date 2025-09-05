package com.crediya.iam.api.config.config;


import com.crediya.iam.api.config.*;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de integración para verificar que todas las clases de configuración
 * están correctamente anotadas y pueden ser detectadas por Spring.
 */
class ConfigurationIntegrationTest {

    @Test
    void allConfigClasses_shouldHaveCorrectAnnotations() {
        // SecurityConfig
        assertTrue(SecurityConfig.class.isAnnotationPresent(Configuration.class));

        // CorsConfig
        assertTrue(CorsConfig.class.isAnnotationPresent(Configuration.class));

        // OpenApiConfig
        assertTrue(OpenApiConfig.class.isAnnotationPresent(Configuration.class));

        // BearerSecurityContextRepository
        assertTrue(BearerSecurityContextRepository.class.isAnnotationPresent(Component.class));

        // SecurityHeadersConfig
        assertTrue(SecurityHeadersConfig.class.isAnnotationPresent(Component.class));
    }

    @Test
    void configClasses_shouldHaveDefaultConstructorsOrProperDependencyInjection() {
        // CorsConfig should have default constructor
        assertDoesNotThrow(() -> new CorsConfig());

        // OpenApiConfig should have default constructor
        assertDoesNotThrow(() -> new OpenApiConfig());

        // SecurityHeadersConfig should have default constructor
        assertDoesNotThrow(() -> new SecurityHeadersConfig());

        // SecurityConfig and BearerSecurityContextRepository require dependencies,
        // so we just verify they don't have default constructors
        assertEquals(1, SecurityConfig.class.getConstructors().length);
        assertEquals(1, BearerSecurityContextRepository.class.getConstructors().length);
    }
}
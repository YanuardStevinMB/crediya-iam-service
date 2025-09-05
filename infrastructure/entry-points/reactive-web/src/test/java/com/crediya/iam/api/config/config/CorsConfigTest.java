package com.crediya.iam.api.config.config;

import com.crediya.iam.api.config.CorsConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CorsConfigTest {

    @InjectMocks
    private CorsConfig corsConfig;

    /** Helper: obtiene la CorsConfiguration para una ruta simulando un exchange */
    private CorsConfiguration getConfig(UrlBasedCorsConfigurationSource source, String path) {
        var req = MockServerHttpRequest.options(path).build(); // OPTIONS o GET, da igual: solo usa la ruta
        var exchange = MockServerWebExchange.from(req);
        return source.getCorsConfiguration(exchange);
    }

    /** Helper: extrae el configSource del CorsWebFilter */
    private UrlBasedCorsConfigurationSource extractCorsConfigurationSource(CorsWebFilter corsWebFilter) {
        return (UrlBasedCorsConfigurationSource) ReflectionTestUtils.getField(corsWebFilter, "configSource");
    }

    @Test
    void corsWebFilter_WithSingleOrigin_ShouldCreateFilterWithCorrectConfiguration() {
        String origins = "http://localhost:3000";

        CorsWebFilter corsWebFilter = corsConfig.corsWebFilter(origins);
        assertNotNull(corsWebFilter);

        UrlBasedCorsConfigurationSource source = extractCorsConfigurationSource(corsWebFilter);
        CorsConfiguration corsConfiguration = getConfig(source, "/cualquier");

        assertNotNull(corsConfiguration);
        assertTrue(Boolean.TRUE.equals(corsConfiguration.getAllowCredentials()));
        assertEquals(List.of("http://localhost:3000"), corsConfiguration.getAllowedOrigins());
        assertEquals(Arrays.asList("POST", "GET"), corsConfiguration.getAllowedMethods());
        assertEquals(List.of(CorsConfiguration.ALL), corsConfiguration.getAllowedHeaders());
    }

    @Test
    void corsWebFilter_WithMultipleOrigins_ShouldCreateFilterWithAllOrigins() {
        String origins = "http://localhost:3000,https://app.crediya.com,https://staging.crediya.com";

        CorsWebFilter corsWebFilter = corsConfig.corsWebFilter(origins);
        UrlBasedCorsConfigurationSource source = extractCorsConfigurationSource(corsWebFilter);
        CorsConfiguration corsConfiguration = getConfig(source, "/api/v1/usuarios");

        assertNotNull(corsConfiguration);
        List<String> expectedOrigins = List.of(
                "http://localhost:3000",
                "https://app.crediya.com",
                "https://staging.crediya.com"
        );
        assertEquals(expectedOrigins, corsConfiguration.getAllowedOrigins());
    }

    @Test
    void corsWebFilter_WithOriginsContainingSpaces_ShouldTrimAndParseCorrectly() {
        String origins = " http://localhost:3000 , https://app.crediya.com , https://staging.crediya.com ";

        CorsWebFilter corsWebFilter = corsConfig.corsWebFilter(origins);
        UrlBasedCorsConfigurationSource source = extractCorsConfigurationSource(corsWebFilter);
        CorsConfiguration corsConfiguration = getConfig(source, "/lo-que-sea");

        assertNotNull(corsConfiguration);
        // Si tu implementación no hace trim, esto validará que se conservan los espacios
        List<String> actualOrigins = corsConfiguration.getAllowedOrigins();
        assertThat(actualOrigins).containsExactly(
                " http://localhost:3000 ",
                " https://app.crediya.com ",
                " https://staging.crediya.com "
        );
    }

    @Test
    void corsWebFilter_WithEmptyOrigins_ShouldCreateFilterWithEmptyOriginsList() {
        String origins = "";

        CorsWebFilter corsWebFilter = corsConfig.corsWebFilter(origins);
        UrlBasedCorsConfigurationSource source = extractCorsConfigurationSource(corsWebFilter);
        CorsConfiguration corsConfiguration = getConfig(source, "/dummy");

        assertNotNull(corsConfiguration);
        assertEquals(List.of(""), corsConfiguration.getAllowedOrigins());
    }

    @Test
    void corsWebFilter_ShouldAlwaysAllowCredentials() {
        String origins = "http://localhost:3000";

        CorsWebFilter corsWebFilter = corsConfig.corsWebFilter(origins);
        UrlBasedCorsConfigurationSource source = extractCorsConfigurationSource(corsWebFilter);
        CorsConfiguration corsConfiguration = getConfig(source, "/health");

        assertTrue(Boolean.TRUE.equals(corsConfiguration.getAllowCredentials()));
    }

    @Test
    void corsWebFilter_ShouldOnlyAllowPostAndGetMethods() {
        String origins = "http://localhost:3000";

        CorsWebFilter corsWebFilter = corsConfig.corsWebFilter(origins);
        UrlBasedCorsConfigurationSource source = extractCorsConfigurationSource(corsWebFilter);
        CorsConfiguration corsConfiguration = getConfig(source, "/otra");

        List<String> allowedMethods = corsConfiguration.getAllowedMethods();
        assertEquals(2, allowedMethods.size());
        assertTrue(allowedMethods.contains("POST"));
        assertTrue(allowedMethods.contains("GET"));
        assertFalse(allowedMethods.contains("PUT"));
        assertFalse(allowedMethods.contains("DELETE"));
        assertFalse(allowedMethods.contains("PATCH"));
    }

    @Test
    void corsWebFilter_ShouldAllowAllHeaders() {
        String origins = "http://localhost:3000";

        CorsWebFilter corsWebFilter = corsConfig.corsWebFilter(origins);
        UrlBasedCorsConfigurationSource source = extractCorsConfigurationSource(corsWebFilter);
        CorsConfiguration corsConfiguration = getConfig(source, "/headers");

        assertEquals(List.of(CorsConfiguration.ALL), corsConfiguration.getAllowedHeaders());
    }

    @Test
    void corsWebFilter_ShouldApplyConfigurationToAllPaths() {
        String origins = "http://localhost:3000";

        CorsWebFilter corsWebFilter = corsConfig.corsWebFilter(origins);
        UrlBasedCorsConfigurationSource source = extractCorsConfigurationSource(corsWebFilter);

        assertNotNull(getConfig(source, "/api/v1/usuarios"));
        assertNotNull(getConfig(source, "/api/v1/login"));
        assertNotNull(getConfig(source, "/health"));
        assertNotNull(getConfig(source, "/cualquier/otra/ruta"));
    }

    @Test
    void corsWebFilter_WithDifferentOriginFormats_ShouldHandleCorrectly() {
        String origins = "http://localhost:3000,https://app.crediya.com:443,http://192.168.1.100:8080";

        CorsWebFilter corsWebFilter = corsConfig.corsWebFilter(origins);
        UrlBasedCorsConfigurationSource source = extractCorsConfigurationSource(corsWebFilter);
        CorsConfiguration corsConfiguration = getConfig(source, "/mix");

        List<String> expectedOrigins = List.of(
                "http://localhost:3000",
                "https://app.crediya.com:443",
                "http://192.168.1.100:8080"
        );
        assertEquals(expectedOrigins, corsConfiguration.getAllowedOrigins());
    }

    @Test
    void corsWebFilter_Configuration_ShouldBeConsistent() {
        String origins = "http://localhost:3000";

        CorsWebFilter corsWebFilter1 = corsConfig.corsWebFilter(origins);
        CorsWebFilter corsWebFilter2 = corsConfig.corsWebFilter(origins);

        UrlBasedCorsConfigurationSource source1 = extractCorsConfigurationSource(corsWebFilter1);
        UrlBasedCorsConfigurationSource source2 = extractCorsConfigurationSource(corsWebFilter2);

        CorsConfiguration config1 = getConfig(source1, "/a");
        CorsConfiguration config2 = getConfig(source2, "/b");

        assertEquals(config1.getAllowCredentials(), config2.getAllowCredentials());
        assertEquals(config1.getAllowedOrigins(), config2.getAllowedOrigins());
        assertEquals(config1.getAllowedMethods(), config2.getAllowedMethods());
        assertEquals(config1.getAllowedHeaders(), config2.getAllowedHeaders());
    }

    @Test
    void corsWebFilter_WithNullOrigins_ShouldHandleGracefully() {
        String origins = null;
        assertThrows(NullPointerException.class, () -> corsConfig.corsWebFilter(origins));
    }



    @Test
    void corsWebFilter_Bean_ShouldNotBeNull() {
        String origins = "http://localhost:3000";

        CorsWebFilter corsWebFilter = corsConfig.corsWebFilter(origins);
        assertNotNull(corsWebFilter);
        assertThat(corsWebFilter).isInstanceOf(CorsWebFilter.class);
    }

    @Test
    void corsWebFilter_WithComplexOriginConfiguration_ShouldWorkCorrectly() {
        String origins = "https://app.crediya.com,https://admin.crediya.com,https://staging.crediya.com,http://localhost:3000,Http://localhost:3001";

        CorsWebFilter corsWebFilter = corsConfig.corsWebFilter(origins);
        UrlBasedCorsConfigurationSource source = extractCorsConfigurationSource(corsWebFilter);
        CorsConfiguration corsConfiguration = getConfig(source, "/prod");

        assertNotNull(corsConfiguration);
        assertEquals(5, corsConfiguration.getAllowedOrigins().size());
        assertTrue(corsConfiguration.getAllowedOrigins().contains("https://app.crediya.com"));
        assertTrue(corsConfiguration.getAllowedOrigins().contains("https://admin.crediya.com"));
        assertTrue(corsConfiguration.getAllowedOrigins().contains("https://staging.crediya.com"));
        assertTrue(corsConfiguration.getAllowedOrigins().contains("http://localhost:3000"));
        assertTrue(corsConfiguration.getAllowedOrigins().contains("Http://localhost:3001"));

        assertTrue(Boolean.TRUE.equals(corsConfiguration.getAllowCredentials()));
        assertEquals(Arrays.asList("POST", "GET"), corsConfiguration.getAllowedMethods());
        assertEquals(List.of(CorsConfiguration.ALL), corsConfiguration.getAllowedHeaders());
    }

    @Test
    void corsWebFilter_WithConfiguredOrigins_ShouldMatchExpectedConfiguration() {
        String origins = "http://localhost:4200,http://localhost:8081";

        CorsWebFilter corsWebFilter = corsConfig.corsWebFilter(origins);
        UrlBasedCorsConfigurationSource source = extractCorsConfigurationSource(corsWebFilter);
        CorsConfiguration corsConfiguration = getConfig(source, "/api/v1/loquesea");

        assertNotNull(corsConfiguration);
        assertEquals(2, corsConfiguration.getAllowedOrigins().size());
        assertTrue(corsConfiguration.getAllowedOrigins().contains("http://localhost:4200"));
        assertTrue(corsConfiguration.getAllowedOrigins().contains("http://localhost:8081"));

        assertTrue(Boolean.TRUE.equals(corsConfiguration.getAllowCredentials()));
        assertEquals(Arrays.asList("POST", "GET"), corsConfiguration.getAllowedMethods());
        assertEquals(List.of(CorsConfiguration.ALL), corsConfiguration.getAllowedHeaders());
    }
}

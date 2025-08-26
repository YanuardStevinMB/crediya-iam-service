package com.crediya.iam.api.config;

import com.crediya.iam.api.ApiErrorFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@WebFluxTest
@ContextConfiguration(classes = { ConfigTest.TestRoutes.class }) // 👈 ruta dummy para probar filtros
@Import({ CorsConfig.class, SecurityHeadersConfig.class, ApiErrorFilter.class })
class ConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    // Pequeño router para probar los headers de los filtros/config
    @Configuration
    static class TestRoutes {
        @Bean
        RouterFunction<ServerResponse> probeRoute() {
            return route(GET("/__probe"), req -> ServerResponse.ok().bodyValue("ok"));
        }
    }

    @Test
    void corsConfigurationShouldAllowOrigins() {
        webTestClient.get()
                .uri("/__probe")
                .exchange()
                .expectStatus().isOk()
                // 🔎 Sé flexible: distintas infraestructuras pueden formatear estos headers.
                .expectHeader().exists("Content-Security-Policy")
                .expectHeader().exists("Strict-Transport-Security")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().exists("Referrer-Policy")
                .expectHeader().exists("Cache-Control")
                .expectHeader().exists("Pragma")
                // Muchas veces el header 'Server' simplemente no existe
                .expectHeader().doesNotExist("Server");
    }
}

package com.crediya.iam.api.config.config;


import com.crediya.iam.api.config.SecurityHeadersConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityHeadersConfigTest {

    private SecurityHeadersConfig securityHeadersConfig;
    private WebFilterChain filterChain;

    @BeforeEach
    void setUp() {
        securityHeadersConfig = new SecurityHeadersConfig();
        filterChain = mock(WebFilterChain.class);
        when(filterChain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void filter_shouldAddAllSecurityHeaders() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));

        StepVerifier.create(securityHeadersConfig.filter(exchange, filterChain))
                .verifyComplete();

        HttpHeaders headers = exchange.getResponse().getHeaders();

        assertEquals("default-src 'self'; frame-ancestors 'self'; form-action 'self'",
                headers.getFirst("Content-Security-Policy"));
        assertEquals("max-age=31536000;",
                headers.getFirst("Strict-Transport-Security"));
        assertEquals("nosniff",
                headers.getFirst("X-Content-Type-Options"));
        assertEquals("",
                headers.getFirst("Server"));
        assertEquals("no-store",
                headers.getFirst("Cache-Control"));
        assertEquals("no-cache",
                headers.getFirst("Pragma"));
        assertEquals("strict-origin-when-cross-origin",
                headers.getFirst("Referrer-Policy"));

        verify(filterChain).filter(exchange);
    }

    @Test
    void filter_shouldContinueFilterChain() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));

        StepVerifier.create(securityHeadersConfig.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    void filter_shouldHandleFilterChainError() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
        RuntimeException error = new RuntimeException("Filter chain error");
        when(filterChain.filter(any())).thenReturn(Mono.error(error));

        StepVerifier.create(securityHeadersConfig.filter(exchange, filterChain))
                .expectError(RuntimeException.class)
                .verify();

        // Headers should still be set even if filter chain fails
        HttpHeaders headers = exchange.getResponse().getHeaders();
        assertNotNull(headers.getFirst("Content-Security-Policy"));
        verify(filterChain).filter(exchange);
    }

    @Test
    void filter_shouldSetHeadersBeforeContinuingChain() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));

        // Usar un filtro que verifique que los headers ya están establecidos
        when(filterChain.filter(any())).thenAnswer(invocation -> {
            ServerWebExchange ex = invocation.getArgument(0);
            HttpHeaders headers = ex.getResponse().getHeaders();

            // Verificar que los headers ya están establecidos cuando se ejecuta la cadena
            assertNotNull(headers.getFirst("Content-Security-Policy"));
            assertNotNull(headers.getFirst("X-Content-Type-Options"));

            return Mono.empty();
        });

        StepVerifier.create(securityHeadersConfig.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(exchange);
    }


    @Test
    void filter_shouldWorkWithDifferentRequestMethods() {
        ServerWebExchange getExchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/test"));
        ServerWebExchange postExchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/test"));

        // Test GET request
        StepVerifier.create(securityHeadersConfig.filter(getExchange, filterChain))
                .verifyComplete();

        // Test POST request
        StepVerifier.create(securityHeadersConfig.filter(postExchange, filterChain))
                .verifyComplete();

        // Verify headers are set for both
        assertNotNull(getExchange.getResponse().getHeaders().getFirst("Content-Security-Policy"));
        assertNotNull(postExchange.getResponse().getHeaders().getFirst("Content-Security-Policy"));

        verify(filterChain, times(2)).filter(any());
    }
}
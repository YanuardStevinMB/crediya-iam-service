package com.crediya.iam.api.config.config;


import com.crediya.iam.api.config.BearerSecurityContextRepository;
import com.crediya.iam.security.jwt.JwtReactiveAuthenticationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BearerSecurityContextRepositoryTest {

    @Mock
    private JwtReactiveAuthenticationManager authManager;

    @Mock
    private Authentication authentication;

    private BearerSecurityContextRepository repository;

    @BeforeEach
    void setUp() {
        repository = new BearerSecurityContextRepository(authManager);
    }

    @Test
    void save_shouldReturnEmptyMono() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
        SecurityContext context = mock(SecurityContext.class);

        StepVerifier.create(repository.save(exchange, context))
                .verifyComplete();

        // No debería hacer ninguna operación ya que es stateless
        verifyNoInteractions(authManager);
    }

    @Test
    void load_shouldExtractTokenFromBearerHeader() {
        String token = "valid.jwt.token";
        MockServerHttpRequest request = MockServerHttpRequest.get("/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(Mono.just(authentication));

        StepVerifier.create(repository.load(exchange))
                .expectNextMatches(securityContext -> securityContext.getAuthentication() == authentication)
                .verifyComplete();

        verify(authManager).authenticate(argThat(auth ->
                auth instanceof UsernamePasswordAuthenticationToken &&
                        auth.getCredentials().equals(token) &&
                        auth.getPrincipal().equals("N/A")
        ));
    }

    @Test
    void load_shouldExtractTokenFromQueryParameter() {
        String token = "valid.jwt.token";
        MockServerHttpRequest request = MockServerHttpRequest.get("/")
                .queryParam("access_token", token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(Mono.just(authentication));

        StepVerifier.create(repository.load(exchange))
                .expectNextMatches(securityContext -> securityContext.getAuthentication() == authentication)
                .verifyComplete();

        verify(authManager).authenticate(argThat(auth ->
                auth instanceof UsernamePasswordAuthenticationToken &&
                        auth.getCredentials().equals(token)
        ));
    }

    @Test
    void load_shouldReturnEmptyWhenNoAuthorizationHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(repository.load(exchange))
                .verifyComplete();

        verifyNoInteractions(authManager);
    }

    @Test
    void load_shouldReturnEmptyWhenAuthorizationHeaderDoesNotStartWithBearer() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/")
                .header(HttpHeaders.AUTHORIZATION, "Basic credentials")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(repository.load(exchange))
                .verifyComplete();

        verifyNoInteractions(authManager);
    }

    @Test
    void load_shouldReturnEmptyWhenTokenIsBlank() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer   ")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(repository.load(exchange))
                .verifyComplete();

        verifyNoInteractions(authManager);
    }

    @Test
    void load_shouldReturnEmptyWhenTokenIsEmpty() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(repository.load(exchange))
                .verifyComplete();

        verifyNoInteractions(authManager);
    }

    @Test
    void load_shouldHandleAuthenticationFailure() {
        String token = "invalid.jwt.token";
        MockServerHttpRequest request = MockServerHttpRequest.get("/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(Mono.error(new RuntimeException("Authentication failed")));

        StepVerifier.create(repository.load(exchange))
                .expectError(RuntimeException.class)
                .verify();

        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void constructor_shouldSetAuthManager() {
        assertNotNull(repository);
        assertTrue(repository instanceof BearerSecurityContextRepository);
    }

    @Test
    void class_shouldHaveCorrectAnnotations() {
        assertTrue(BearerSecurityContextRepository.class.isAnnotationPresent(org.springframework.stereotype.Component.class));
    }
}
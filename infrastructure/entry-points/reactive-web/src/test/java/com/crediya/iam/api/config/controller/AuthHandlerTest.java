package com.crediya.iam.api.config.controller;

import com.crediya.iam.api.controller.AuthHandler;
import com.crediya.iam.api.dto.ErrorDto;
import com.crediya.iam.api.dto.LoginRequestDto;
import com.crediya.iam.api.dto.LoginResponseDto;
import com.crediya.iam.api.userMapper.UserMapper;
import com.crediya.iam.usecase.authenticate.AuthenticateUseCase;
import com.crediya.iam.usecase.authenticate.TokenResult;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthHandlerTest {

    private AuthenticateUseCase authenticate;
    private UserMapper mapper;
    private Validator validator;
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        authenticate = mock(AuthenticateUseCase.class);
        mapper = mock(UserMapper.class);
        validator = mock(Validator.class);

        var handler = new AuthHandler(authenticate, mapper, validator);

        webTestClient = WebTestClient.bindToRouterFunction(
                org.springframework.web.reactive.function.server.RouterFunctions.route()
                        .POST("/api/v1/login", handler::login)
                        .build()
        ).build();
    }

    @Test
    void login_success_returnsOkWithToken() {
        var token = new TokenResult("abc123", "Bearer", Instant.now().getEpochSecond() + 3600);

        when(authenticate.login("john@example.com", "secret"))
                .thenReturn(Mono.just(token));

        webTestClient.post()
                .uri("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginRequestDto("john@example.com", "secret"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginResponseDto.class)
                .value(body -> {
                    assertEquals("abc123", body.accessToken());   // ✅ accessToken correcto
                    assertEquals("Bearer", body.tokenType());     // ✅ tokenType correcto
                    assertTrue(body.expiresAt() > 0);             // ✅ expiresAt válido
                });
    }

    @Test
    void login_invalidCredentials_returns401() {
        when(authenticate.login(anyString(), anyString()))
                .thenReturn(Mono.error(new IllegalArgumentException("bad creds")));

        webTestClient.post()
                .uri("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginRequestDto("wrong@example.com", "badpass"))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorDto.class)
                .value(err -> assertEquals("invalid_credentials", err.error())); // ✅ usa error()
    }

    @Test
    void login_inactiveUser_returns403() {
        when(authenticate.login(anyString(), anyString()))
                .thenReturn(Mono.error(new IllegalStateException("inactive")));

        webTestClient.post()
                .uri("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginRequestDto("inactive@example.com", "pass"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(ErrorDto.class)
                .value(err -> assertEquals("inactive_user", err.error())); // ✅ usa error()
    }
}

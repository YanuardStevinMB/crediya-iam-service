package com.crediya.iam.api.config.controller;

import com.crediya.iam.api.controller.AuthHandler;
import com.crediya.iam.api.dto.LoginRequestDto;
import com.crediya.iam.api.userMapper.UserMapper;
import com.crediya.iam.usecase.authenticate.AuthenticateUseCase;
import com.crediya.iam.usecase.authenticate.TokenResult;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

class AuthHandlerTest {

    private AuthenticateUseCase authenticate;
    private UserMapper mapper;
    private Validator validator;
    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        authenticate = mock(AuthenticateUseCase.class);
        mapper = mock(UserMapper.class);
        validator = mock(Validator.class);

        AuthHandler authHandler = new AuthHandler(authenticate, mapper, validator);

        var router = route(POST("/api/v1/login"), authHandler::login);
        webTestClient = WebTestClient.bindToRouterFunction(router).build();
    }

    @Test
    void login_ok() {
        // Arrange
        TokenResult token = new TokenResult("abc123", "Bearer", 9999L);
        when(authenticate.login(anyString(), anyString()))
                .thenReturn(Mono.just(token));

        LoginRequestDto req = new LoginRequestDto("test@mail.com", "secret");

        // Act + Assert
        webTestClient.post()
                .uri("/api/v1/login")
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody();
    }

    @Test
    void login_invalidCredentials() {
        when(authenticate.login(anyString(), anyString()))
                .thenReturn(Mono.error(new IllegalArgumentException("bad creds")));

        LoginRequestDto req = new LoginRequestDto("wrong@mail.com", "bad");

        webTestClient.post()
                .uri("/api/v1/login")
                .bodyValue(req)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody();
    }

    @Test
    void login_inactiveUser() {
        when(authenticate.login(anyString(), anyString()))
                .thenReturn(Mono.error(new IllegalStateException("inactive")));

        LoginRequestDto req = new LoginRequestDto("inactive@mail.com", "pass");

        webTestClient.post()
                .uri("/api/v1/login")
                .bodyValue(req)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody();
    }
}

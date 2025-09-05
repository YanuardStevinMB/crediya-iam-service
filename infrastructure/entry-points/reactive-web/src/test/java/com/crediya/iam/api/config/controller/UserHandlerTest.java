package com.crediya.iam.api.config.controller;

import com.crediya.iam.api.controller.UserHandler;
import com.crediya.iam.api.dto.UserResponseDto;
import com.crediya.iam.api.dto.UserSaveDto;
import com.crediya.iam.api.userMapper.UserMapper;
import com.crediya.iam.model.user.User;
import com.crediya.iam.usecase.loadusers.LoadUsersUseCase;
import com.crediya.iam.usecase.user.IUserUseCase;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

class UserHandlerTest {

    private IUserUseCase userUseCase;
    private LoadUsersUseCase loadUsersUseCase;
    private UserMapper mapper;
    private Validator validator;
    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        userUseCase = mock(IUserUseCase.class);
        loadUsersUseCase = mock(LoadUsersUseCase.class);
        mapper = mock(UserMapper.class);
        validator = mock(Validator.class);

        UserHandler userHandler = new UserHandler(userUseCase, loadUsersUseCase, mapper, validator);

        // 🚀 Router reducido solo con UserHandler
        var routerFunction = route(GET("/api/v1/usuarios"), userHandler::list)
                .andRoute(POST("/api/v1/usuarios"), userHandler::save);

        webTestClient = WebTestClient.bindToRouterFunction(routerFunction).build();
    }

    private UserSaveDto buildValidDto() {
        return new UserSaveDto(
                null,
                "Juan",
                "Pérez",
                "juan.perez@mail.com",
                LocalDate.of(1995, 5, 20),
                "123456789",
                "3001234567",
                new BigDecimal("2500000.00"),
                "Calle Falsa 123",
                "superSecret",
                1L
        );
    }

    @Test
    void listUsers_ok() {
        User user = new User();
        user.setId(1L);

        when(loadUsersUseCase.execute()).thenReturn(Flux.just(user));

        webTestClient.get()
                .uri("/api/v1/usuarios")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody();
        verify(loadUsersUseCase).execute();
    }

    @Test
    void saveUser_ok() {
        UserSaveDto dto = buildValidDto();
        User model = new User();
        model.setId(1L);
        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(1L);

        when(validator.validate(any(UserSaveDto.class))).thenReturn(Collections.emptySet());
        when(mapper.toModel(dto)).thenReturn(model);
        when(userUseCase.execute(model)).thenReturn(Mono.just(model));
        when(mapper.toResponseDto(model)).thenReturn(responseDto);

        webTestClient.post()
                .uri("/api/v1/usuarios")
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()

                .jsonPath("$.data.id").isEqualTo(1);

        verify(userUseCase).execute(model);
    }

    @Test
    void saveUser_shouldFailValidation() {
        // DTO inválido: email vacío
        UserSaveDto invalid = new UserSaveDto(
                null,
                "Ana",
                "López",
                "", // email inválido
                LocalDate.of(2010, 1, 1),
                "12",
                "abc",
                new BigDecimal("1000"),
                "Direccion",
                "123",
                1L
        );

        webTestClient.post()
                .uri("/api/v1/usuarios")
                .bodyValue(invalid)
                .exchange()
                .expectStatus().is5xxServerError(); // por ConstraintViolationException en validate()
    }
}

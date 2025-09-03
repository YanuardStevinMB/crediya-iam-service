//package com.crediya.iam.api.config.controller;
//
//import com.crediya.iam.api.controller.UserHandler;
//import com.crediya.iam.api.dto.ApiResponse;
//import com.crediya.iam.api.dto.UserResponseDto;
//import com.crediya.iam.api.dto.UserSaveDto;
//import com.crediya.iam.api.userMapper.UserMapper;
//import com.crediya.iam.usecase.user.IUserUseCase;
//import jakarta.validation.ConstraintViolation;
//import jakarta.validation.ConstraintViolationException;
//import jakarta.validation.Path;
//import jakarta.validation.Validator;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.reactive.server.WebTestClient;
//import reactor.core.publisher.Mono;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.Set;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//class UserHandlerTest {
//
//    private IUserUseCase useCase;
//    private UserMapper mapper;
//    private Validator validator;
//    private WebTestClient webTestClient;
//
//    @BeforeEach
//    void setUp() {
//        useCase = mock(IUserUseCase.class);
//        mapper = mock(UserMapper.class);
//        validator = mock(Validator.class);
//
//        var handler = new UserHandler(useCase, mapper, validator);
//
//        webTestClient = WebTestClient.bindToRouterFunction(
//                org.springframework.web.reactive.function.server.RouterFunctions.route()
//                        .POST("/api/v1/usuarios", handler::save)
//                        .build()
//        ).build();
//    }
//
//    private UserSaveDto buildDto() {
//        return new UserSaveDto(
//                1L,
//                "Alice",
//                "Smith",
//                "alice@example.com",
//                LocalDate.of(1990, 1, 1),
//                "DOC123",
//                "5551234",
//                new BigDecimal("5000.00"),
//                "Main St",
//                "pass123",
//                2L
//        );
//    }
//
//    @Test
//    void save_success_returns200() {
//        var dto = buildDto();
//
//        var model = new Object(); // cualquier objeto simulado de dominio
//        var responseDto = new UserResponseDto(
//                1L, "Alice", "Smith", "alice@example.com",
//                LocalDate.of(1990, 1, 1), "DOC123", "5551234",
//                new BigDecimal("5000.00"), "Main St", 2L
//        );
//
//        when(validator.validate(any())).thenReturn(Set.of());
//        when(mapper.toModel(dto)).thenReturn(model);
//        when(useCase.execute(model)).thenReturn(Mono.just(new Object() {
//            public Long getId() { return 1L; }
//        }));
//        when(mapper.toResponseDto(any())).thenReturn(responseDto);
//
//        webTestClient.post()
//                .uri("/api/v1/usuarios")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(dto)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(ApiResponse.class)
//                .value(resp -> {
//                    assert resp.isSuccess();
//                    assert resp.getMessage().equals("Usuario creado correctamente");
//                });
//    }
//
//    @Test
//    void save_validationError_returns400() {
//        var dto = buildDto();
//
//        @SuppressWarnings("unchecked")
//        ConstraintViolation<Object> violation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
//
//        Path path = () -> "email";
//        when(violation.getPropertyPath()).thenReturn(path);
//        when(violation.getMessage()).thenReturn("Email inválido");
//
//        when(validator.validate(any())).thenReturn(Set.of(violation));
//
//        webTestClient.post()
//                .uri("/api/v1/usuarios")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(dto)
//                .exchange()
//                .expectStatus().isBadRequest()
//                .expectBody(ApiResponse.class)
//                .value(resp -> {
//                    assert !resp.isSuccess();
//                    assert resp.getMessage().equals("Validación fallida");
//                });
//    }
//
//    @Test
//    void save_unexpectedError_returns500() {
//        var dto = buildDto();
//
//        when(validator.validate(any())).thenReturn(Set.of());
//        when(mapper.toModel(dto)).thenReturn(new Object());
//        when(useCase.execute(any())).thenReturn(Mono.error(new RuntimeException("DB error")));
//
//        webTestClient.post()
//                .uri("/api/v1/usuarios")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(dto)
//                .exchange()
//                .expectStatus().is5xxServerError()
//                .expectBody(ApiResponse.class)
//                .value(resp -> {
//                    assert !resp.isSuccess();
//                    assert resp.getMessage().equals("Error interno del servidor");
//                });
//    }
//}

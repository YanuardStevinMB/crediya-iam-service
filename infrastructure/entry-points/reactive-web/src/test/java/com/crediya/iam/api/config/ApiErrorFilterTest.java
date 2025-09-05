package com.crediya.iam.api.config;

import com.crediya.iam.api.ApiErrorFilter;
import com.crediya.iam.api.ApiErrorUtils;
import com.crediya.iam.usecase.user.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiErrorFilterTest {

    @Mock
    private HandlerFunction<ServerResponse> handlerFunction;

    @Mock
    private ServerRequest serverRequest;

    @Mock
    private ServerResponse serverResponse;

    private ApiErrorFilter apiErrorFilter;

    @BeforeEach
    void setUp() {
        apiErrorFilter = new ApiErrorFilter();
    }

    @Test
    void filter_WhenNoError_ShouldReturnOriginalResponse() {
        // Given
        when(handlerFunction.handle(serverRequest)).thenReturn(Mono.just(serverResponse));

        // When
        Mono<ServerResponse> result = apiErrorFilter.filter(serverRequest, handlerFunction);

        // Then
        StepVerifier.create(result)
                .expectNext(serverResponse)
                .verifyComplete();

        verify(handlerFunction).handle(serverRequest);
    }

    @Test
    void filter_WhenEmptyMono_ShouldReturnNoContent() {
        // Given
        when(handlerFunction.handle(serverRequest)).thenReturn(Mono.empty());

        // When
        Mono<ServerResponse> result = apiErrorFilter.filter(serverRequest, handlerFunction);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response -> response != null)
                .verifyComplete();
    }

    @Test
    void filter_WhenEmailDuplicadoException_ShouldReturnConflict() {
        // Given
        EmailDuplicadoException exception = new EmailDuplicadoException("test@example.com");
        when(handlerFunction.handle(serverRequest)).thenReturn(Mono.error(exception));

        try (MockedStatic<ApiErrorUtils> mockedStatic = mockStatic(ApiErrorUtils.class)) {
            when(ApiErrorUtils.respond(eq(serverRequest), eq(HttpStatus.CONFLICT), eq("Email duplicado"), any()))
                    .thenReturn(Mono.just(serverResponse));

            // When
            Mono<ServerResponse> result = apiErrorFilter.filter(serverRequest, handlerFunction);

            // Then
            StepVerifier.create(result)
                    .expectNext(serverResponse)
                    .verifyComplete();

            mockedStatic.verify(() -> ApiErrorUtils.respond(
                    eq(serverRequest),
                    eq(HttpStatus.CONFLICT),
                    eq("Email duplicado"),
                    any()
            ));
        }
    }

    @Test
    void filter_WhenUserAlreadyExistsException_ShouldReturnConflict() {
        // Given
        UserAlreadyExistsException exception = new UserAlreadyExistsException("test@example.com");
        when(handlerFunction.handle(serverRequest)).thenReturn(Mono.error(exception));

        try (MockedStatic<ApiErrorUtils> mockedStatic = mockStatic(ApiErrorUtils.class)) {
            when(ApiErrorUtils.respond(eq(serverRequest), eq(HttpStatus.CONFLICT), eq("Email duplicado"), any()))
                    .thenReturn(Mono.just(serverResponse));

            // When
            Mono<ServerResponse> result = apiErrorFilter.filter(serverRequest, handlerFunction);

            // Then
            StepVerifier.create(result)
                    .expectNext(serverResponse)
                    .verifyComplete();
        }
    }

    @Test
    void filter_WhenRoleNotFoundException_ShouldReturnNotFound() {
        // Given
        RoleNotFoundException exception = new RoleNotFoundException(4l);
        when(handlerFunction.handle(serverRequest)).thenReturn(Mono.error(exception));

        try (MockedStatic<ApiErrorUtils> mockedStatic = mockStatic(ApiErrorUtils.class)) {
            when(ApiErrorUtils.respond(eq(serverRequest), eq(HttpStatus.NOT_FOUND),
                    eq("El rol especificado no existe"), any()))
                    .thenReturn(Mono.just(serverResponse));

            // When
            Mono<ServerResponse> result = apiErrorFilter.filter(serverRequest, handlerFunction);

            // Then
            StepVerifier.create(result)
                    .expectNext(serverResponse)
                    .verifyComplete();
        }
    }

    @Test
    void filter_WhenSalaryValidateException_ShouldReturnBadRequest() {
        // Given
        SalaryValidateException exception = new SalaryValidateException("Salario debe ser positivo");
        when(handlerFunction.handle(serverRequest)).thenReturn(Mono.error(exception));

        try (MockedStatic<ApiErrorUtils> mockedStatic = mockStatic(ApiErrorUtils.class)) {
            when(ApiErrorUtils.respond(eq(serverRequest), eq(HttpStatus.BAD_REQUEST),
                    eq("Salario inválido"), eq("Salario debe ser positivo")))
                    .thenReturn(Mono.just(serverResponse));

            // When
            Mono<ServerResponse> result = apiErrorFilter.filter(serverRequest, handlerFunction);

            // Then
            StepVerifier.create(result)
                    .expectNext(serverResponse)
                    .verifyComplete();
        }
    }



    @Test
    void filter_WhenIllegalArgumentException_ShouldReturnBadRequest() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Argumento inválido");
        when(handlerFunction.handle(serverRequest)).thenReturn(Mono.error(exception));

        try (MockedStatic<ApiErrorUtils> mockedStatic = mockStatic(ApiErrorUtils.class)) {
            when(ApiErrorUtils.respond(eq(serverRequest), eq(HttpStatus.BAD_REQUEST),
                    eq("Solicitud inválida"), eq("Argumento inválido")))
                    .thenReturn(Mono.just(serverResponse));

            // When
            Mono<ServerResponse> result = apiErrorFilter.filter(serverRequest, handlerFunction);

            // Then
            StepVerifier.create(result)
                    .expectNext(serverResponse)
                    .verifyComplete();
        }
    }

    @Test
    void filter_WhenWrappedEmailDuplicadoException_ShouldReturnConflict() {
        // Given
        EmailDuplicadoException wrappedException = new EmailDuplicadoException("test@example.com");
        RuntimeException wrapperException = new RuntimeException(wrappedException);
        when(handlerFunction.handle(serverRequest)).thenReturn(Mono.error(wrapperException));

        try (MockedStatic<ApiErrorUtils> mockedStatic = mockStatic(ApiErrorUtils.class);
             MockedStatic<Exceptions> exceptionsStatic = mockStatic(Exceptions.class)) {

            exceptionsStatic.when(() -> Exceptions.unwrap(wrapperException)).thenReturn(wrappedException);
            when(ApiErrorUtils.respond(eq(serverRequest), eq(HttpStatus.CONFLICT),
                    eq("Email duplicado"), any()))
                    .thenReturn(Mono.just(serverResponse));

            // When
            Mono<ServerResponse> result = apiErrorFilter.filter(serverRequest, handlerFunction);

            // Then
            StepVerifier.create(result)
                    .expectNext(serverResponse)
                    .verifyComplete();
        }
    }

    @Test
    void filter_WhenWrappedRoleNotFoundException_ShouldReturnNotFound() {
        // Given
        RoleNotFoundException wrappedException = new RoleNotFoundException(4l);
        RuntimeException wrapperException = new RuntimeException(wrappedException);
        when(handlerFunction.handle(serverRequest)).thenReturn(Mono.error(wrapperException));

        try (MockedStatic<ApiErrorUtils> mockedStatic = mockStatic(ApiErrorUtils.class);
             MockedStatic<Exceptions> exceptionsStatic = mockStatic(Exceptions.class)) {

            exceptionsStatic.when(() -> Exceptions.unwrap(wrapperException)).thenReturn(wrappedException);
            when(ApiErrorUtils.respond(eq(serverRequest), eq(HttpStatus.NOT_FOUND),
                    eq("El rol especificado no existe"), any()))
                    .thenReturn(Mono.just(serverResponse));

            // When
            Mono<ServerResponse> result = apiErrorFilter.filter(serverRequest, handlerFunction);

            // Then
            StepVerifier.create(result)
                    .expectNext(serverResponse)
                    .verifyComplete();
        }
    }

    @Test
    void filter_WhenWrappedSalaryValidateException_ShouldReturnBadRequest() {
        // Given
        SalaryValidateException wrappedException = new SalaryValidateException("Salario inválido");
        RuntimeException wrapperException = new RuntimeException(wrappedException);
        when(handlerFunction.handle(serverRequest)).thenReturn(Mono.error(wrapperException));

        try (MockedStatic<ApiErrorUtils> mockedStatic = mockStatic(ApiErrorUtils.class);
             MockedStatic<Exceptions> exceptionsStatic = mockStatic(Exceptions.class)) {

            exceptionsStatic.when(() -> Exceptions.unwrap(wrapperException)).thenReturn(wrappedException);
            when(ApiErrorUtils.respond(eq(serverRequest), eq(HttpStatus.BAD_REQUEST),
                    eq("Salario inválido"), eq("Salario inválido")))
                    .thenReturn(Mono.just(serverResponse));

            // When
            Mono<ServerResponse> result = apiErrorFilter.filter(serverRequest, handlerFunction);

            // Then
            StepVerifier.create(result)
                    .expectNext(serverResponse)
                    .verifyComplete();
        }
    }

    @Test
    void filter_WhenWrappedIllegalArgumentException_ShouldReturnBadRequest() {
        // Given
        IllegalArgumentException wrappedException = new IllegalArgumentException("Argumento inválido");
        RuntimeException wrapperException = new RuntimeException(wrappedException);
        when(handlerFunction.handle(serverRequest)).thenReturn(Mono.error(wrapperException));

        try (MockedStatic<ApiErrorUtils> mockedStatic = mockStatic(ApiErrorUtils.class);
             MockedStatic<Exceptions> exceptionsStatic = mockStatic(Exceptions.class)) {

            exceptionsStatic.when(() -> Exceptions.unwrap(wrapperException)).thenReturn(wrappedException);
            when(ApiErrorUtils.respond(eq(serverRequest), eq(HttpStatus.BAD_REQUEST),
                    eq("Solicitud inválida"), eq("Argumento inválido")))
                    .thenReturn(Mono.just(serverResponse));

            // When
            Mono<ServerResponse> result = apiErrorFilter.filter(serverRequest, handlerFunction);

            // Then
            StepVerifier.create(result)
                    .expectNext(serverResponse)
                    .verifyComplete();
        }
    }

    @Test
    void filter_WhenWrappedUnknownException_ShouldRethrowException() {
        // Given
        NullPointerException wrappedException = new NullPointerException("Unknown error");
        RuntimeException wrapperException = new RuntimeException(wrappedException);
        when(handlerFunction.handle(serverRequest)).thenReturn(Mono.error(wrapperException));

        try (MockedStatic<ApiErrorUtils> mockedStatic = mockStatic(ApiErrorUtils.class);
             MockedStatic<Exceptions> exceptionsStatic = mockStatic(Exceptions.class)) {

            exceptionsStatic.when(() -> Exceptions.unwrap(wrapperException)).thenReturn(wrappedException);
            when(ApiErrorUtils.respond(eq(serverRequest), eq(HttpStatus.INTERNAL_SERVER_ERROR),
                    eq("Ocurrió un error inesperado"), eq(null)))
                    .thenReturn(Mono.just(serverResponse));

            // When
            Mono<ServerResponse> result = apiErrorFilter.filter(serverRequest, handlerFunction);

            // Then
            StepVerifier.create(result)
                    .expectNext(serverResponse)
                    .verifyComplete();
        }
    }

    @Test
    void filter_WhenUnknownException_ShouldReturnInternalServerError() {
        // Given
        RuntimeException exception = new RuntimeException("Error desconocido");
        when(handlerFunction.handle(serverRequest)).thenReturn(Mono.error(exception));

        try (MockedStatic<ApiErrorUtils> mockedStatic = mockStatic(ApiErrorUtils.class)) {
            when(ApiErrorUtils.respond(eq(serverRequest), eq(HttpStatus.INTERNAL_SERVER_ERROR),
                    eq("Ocurrió un error inesperado"), eq(null)))
                    .thenReturn(Mono.just(serverResponse));

            // When
            Mono<ServerResponse> result = apiErrorFilter.filter(serverRequest, handlerFunction);

            // Then
            StepVerifier.create(result)
                    .expectNext(serverResponse)
                    .verifyComplete();
        }
    }

    @Test
    void filter_WhenExceptionInCatchAll_ShouldHandleGracefully() {
        // Given
        Exception exception = new Exception("Error genérico");
        when(handlerFunction.handle(serverRequest)).thenReturn(Mono.error(exception));

        try (MockedStatic<ApiErrorUtils> mockedStatic = mockStatic(ApiErrorUtils.class)) {
            when(ApiErrorUtils.respond(eq(serverRequest), eq(HttpStatus.INTERNAL_SERVER_ERROR),
                    eq("Ocurrió un error inesperado"), eq(null)))
                    .thenReturn(Mono.just(serverResponse));

            // When
            Mono<ServerResponse> result = apiErrorFilter.filter(serverRequest, handlerFunction);

            // Then
            StepVerifier.create(result)
                    .expectNext(serverResponse)
                    .verifyComplete();
        }
    }
}
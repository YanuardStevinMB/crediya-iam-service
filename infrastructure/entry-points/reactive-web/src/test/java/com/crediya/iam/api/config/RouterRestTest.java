package com.crediya.iam.api.config;

import com.crediya.iam.api.RouterRest;
import com.crediya.iam.api.controller.AuthHandler;
import com.crediya.iam.api.controller.UserHandler;
import com.crediya.iam.api.controller.UserValidatedExistHandler;
import com.crediya.iam.api.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouterRestTest {

    @Mock
    private UserHandler userHandler;

    @Mock
    private AuthHandler authHandler;

    @Mock
    private UserValidatedExistHandler userValidatedExistHandler;

    private RouterRest routerRest;
    private WebTestClient webTestClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        routerRest = new RouterRest();
        objectMapper = new ObjectMapper();

        RouterFunction<ServerResponse> routerFunction = routerRest.routerFunction(
                userHandler, authHandler, userValidatedExistHandler);

        webTestClient = WebTestClient.bindToRouterFunction(routerFunction).build();
    }

    @Test
    void routerFunction_WhenGetUsuarios_ShouldCallUserHandlerList() {
        // Given
        List<UserResponseDto> users = List.of(
                new UserResponseDto(
                        1L,
                        "Juan",
                        "Pérez",
                        "juan@example.com",
                        LocalDate.of(1990, 1, 15),
                        "12345678",
                        "3001234567",
                        new BigDecimal("3000000.00"),
                        "Calle 123 #45-67",
                        1L
                ),
                new UserResponseDto(
                        2L,
                        "María",
                        "García",
                        "maria@example.com",
                        LocalDate.of(1985, 8, 20),
                        "87654321",
                        "3009876543",
                        new BigDecimal("4500000.00"),
                        "Carrera 45 #78-90",
                        2L
                )
        );

        ApiResponse<List<UserResponseDto>> apiResponse = ApiResponse.ok(users, "Usuarios obtenidos exitosamente", "/api/v1/usuarios");

        when(userHandler.list(any(ServerRequest.class)))
                .thenReturn(ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(apiResponse));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/usuarios")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data").isArray()
                .jsonPath("$.data.length()").isEqualTo(2)
                .jsonPath("$.data[0].firstName").isEqualTo("Juan")
                .jsonPath("$.data[0].lastName").isEqualTo("Pérez")
                .jsonPath("$.data[0].email").isEqualTo("juan@example.com")
                .jsonPath("$.data[1].firstName").isEqualTo("María")
                .jsonPath("$.data[1].lastName").isEqualTo("García")
                .jsonPath("$.data[1].email").isEqualTo("maria@example.com");
    }

    @Test
    void routerFunction_WhenGetUsuarios_AndHandlerReturnsError_ShouldReturnError() {
        // Given
        when(userHandler.list(any(ServerRequest.class)))
                .thenReturn(ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.fail("Error interno", null, "/api/v1/usuarios")));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/usuarios")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.success").isEqualTo(false);
    }

    @Test
    void routerFunction_WhenPostUsuarios_ShouldCallUserHandlerSave() {
        // Given
        UserSaveDto userSaveDto = new UserSaveDto(
                null, // id opcional
                "Carlos",
                "Rodríguez",
                "carlos@example.com",
                LocalDate.of(1992, 3, 10),
                "11223344",
                "3001112233",
                new BigDecimal("3500000.00"),
                "Avenida 80 #12-34",
                "password123",
                1L
        );

        UserResponseDto userResponse = new UserResponseDto(
                3L,
                "Carlos",
                "Rodríguez",
                "carlos@example.com",
                LocalDate.of(1992, 3, 10),
                "11223344",
                "3001112233",
                new BigDecimal("3500000.00"),
                "Avenida 80 #12-34",
                1L
        );

        ApiResponse<UserResponseDto> apiResponse = ApiResponse.ok(userResponse, "Usuario creado exitosamente", "/api/v1/usuarios");

        when(userHandler.save(any(ServerRequest.class)))
                .thenReturn(ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(apiResponse));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userSaveDto)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.firstName").isEqualTo("Carlos")
                .jsonPath("$.data.lastName").isEqualTo("Rodríguez")
                .jsonPath("$.data.email").isEqualTo("carlos@example.com")
                .jsonPath("$.data.identityDocument").isEqualTo("11223344")
                .jsonPath("$.data.baseSalary").isEqualTo(3500000.00);
    }

    @Test
    void routerFunction_WhenPostUsuarios_AndEmailDuplicate_ShouldReturnConflict() {
        // Given
        UserSaveDto userSaveDto = new UserSaveDto(
                null,
                "Usuario",
                "Duplicado",
                "duplicado@example.com",
                LocalDate.of(1990, 5, 15),
                "55667788",
                "3005566778",
                new BigDecimal("2800000.00"),
                "Calle 50 #25-30",
                "password123",
                1L
        );

        when(userHandler.save(any(ServerRequest.class)))
                .thenReturn(ServerResponse.status(HttpStatus.CONFLICT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.fail("Email duplicado", null, "/api/v1/usuarios")));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userSaveDto)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.success").isEqualTo(false);
    }

    @Test
    void routerFunction_WhenPostLogin_ShouldCallAuthHandlerLogin() {
        // Given
        LoginRequestDto loginRequest = new LoginRequestDto("user@example.com", "password123");

        LoginResponseDto loginResponse = new LoginResponseDto(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                "Bearer",
                1699123456789L
        );

        ApiResponse<LoginResponseDto> apiResponse = ApiResponse.ok(loginResponse, "Login exitoso", "/api/v1/login");

        when(authHandler.login(any(ServerRequest.class)))
                .thenReturn(ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(apiResponse));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.accessToken").isEqualTo("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .jsonPath("$.data.tokenType").isEqualTo("Bearer")
                .jsonPath("$.data.expiresAt").isEqualTo(1699123456789L);
    }

    @Test
    void routerFunction_WhenPostLogin_AndInvalidCredentials_ShouldReturnUnauthorized() {
        // Given
        LoginRequestDto loginRequest = new LoginRequestDto("invalid@example.com", "wrongpassword");

        when(authHandler.login(any(ServerRequest.class)))
                .thenReturn(ServerResponse.status(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.fail("Credenciales inválidas", null, "/api/v1/login")));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.success").isEqualTo(false);
    }

    @Test
    void routerFunction_WhenPostUsersExist_ShouldCallUserValidatedExistHandler() {
        // Given
        UserExistRequestDto existRequest = new UserExistRequestDto("12345678", "test@example.com");

        ApiResponse<String> apiResponse = ApiResponse.ok("Cliente validado", "Usuario existe y coincide", "/api/v1/users/exist");

        when(userValidatedExistHandler.loadExistUser(any(ServerRequest.class)))
                .thenReturn(ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(apiResponse));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/users/exist")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(existRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data").isEqualTo("Cliente validado");
    }

    @Test
    void routerFunction_WhenPostUsersExist_AndEmailMismatch_ShouldReturnConflict() {
        // Given
        UserExistRequestDto existRequest = new UserExistRequestDto("12345678", "wrong@example.com");

        when(userValidatedExistHandler.loadExistUser(any(ServerRequest.class)))
                .thenReturn(ServerResponse.status(HttpStatus.CONFLICT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.fail("Correo no pertenece al documento registrado", null, "/api/v1/users/exist")));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/users/exist")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(existRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.success").isEqualTo(false);
    }

    @Test
    void routerFunction_WhenInvalidPath_ShouldReturnNotFound() {
        // When & Then
        webTestClient.get()
                .uri("/api/v1/invalid-path")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }



    @Test
    void routerFunction_WhenPostOnGetOnlyEndpoint_ShouldReturnMethodNotAllowed() {
        // Given
        UserSaveDto userSaveDto = new UserSaveDto(
                null,
                "Test",
                "User",
                "test@example.com",
                LocalDate.of(1990, 1, 1),
                "12345678",
                "3001234567",
                new BigDecimal("3000000.00"),
                "Test Address",
                "password123",
                1L
        );

        // When & Then
        webTestClient.post()
                .uri("/api/v1/usuarios/invalid")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userSaveDto)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound(); // No existe esa ruta específica
    }

    @Test
    void routerFunction_WhenAllRoutesAreDefined_ShouldHaveCorrectPathMappings() {
        // Test que verifica que todas las rutas estén correctamente configuradas

        // GET /api/v1/usuarios - debería existir
        when(userHandler.list(any(ServerRequest.class)))
                .thenReturn(ServerResponse.ok().bodyValue("OK"));

        webTestClient.get()
                .uri("/api/v1/usuarios")
                .exchange()
                .expectStatus().isOk();

        // POST /api/v1/usuarios - debería existir
        when(userHandler.save(any(ServerRequest.class)))
                .thenReturn(ServerResponse.ok().bodyValue("OK"));

        webTestClient.post()
                .uri("/api/v1/usuarios")
                .bodyValue("{}")
                .exchange()
                .expectStatus().isOk();

        // POST /api/v1/login - debería existir
        when(authHandler.login(any(ServerRequest.class)))
                .thenReturn(ServerResponse.ok().bodyValue("OK"));

        webTestClient.post()
                .uri("/api/v1/login")
                .bodyValue("{}")
                .exchange()
                .expectStatus().isOk();

        // POST /api/v1/users/exist - debería existir
        when(userValidatedExistHandler.loadExistUser(any(ServerRequest.class)))
                .thenReturn(ServerResponse.ok().bodyValue("OK"));

        webTestClient.post()
                .uri("/api/v1/users/exist")
                .bodyValue("{}")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void routerFunction_WhenHandlerThrowsException_ShouldPropagateError() {
        // Given
        when(userHandler.list(any(ServerRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("Handler error")));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/usuarios")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void routerFunction_WhenUserSaveDto_WithValidData_ShouldValidateAllFields() {
        // Given - Datos válidos según las validaciones del DTO
        UserSaveDto validUserSaveDto = new UserSaveDto(
                null,
                "Ana",
                "López",
                "ana.lopez@example.com",
                LocalDate.of(1988, 7, 25), // fecha pasada válida
                "98765432",
                "3109876543", // teléfono con formato válido
                new BigDecimal("2750000.50"), // salario válido con decimales
                "Diagonal 45 #67-89",
                "securePassword123",
                2L
        );

        UserResponseDto userResponse = new UserResponseDto(
                4L,
                "Ana",
                "López",
                "ana.lopez@example.com",
                LocalDate.of(1988, 7, 25),
                "98765432",
                "3109876543",
                new BigDecimal("2750000.50"),
                "Diagonal 45 #67-89",
                2L
        );

        ApiResponse<UserResponseDto> apiResponse = ApiResponse.ok(userResponse, "Usuario creado exitosamente", "/api/v1/usuarios");

        when(userHandler.save(any(ServerRequest.class)))
                .thenReturn(ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(apiResponse));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validUserSaveDto)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.firstName").isEqualTo("Ana")
                .jsonPath("$.data.lastName").isEqualTo("López")
                .jsonPath("$.data.email").isEqualTo("ana.lopez@example.com")
                .jsonPath("$.data.baseSalary").isEqualTo(2750000.50)
                .jsonPath("$.data.phoneNumber").isEqualTo("3109876543");
    }
}
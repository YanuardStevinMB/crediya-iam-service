package com.crediya.iam.api;

import com.crediya.iam.usecase.user.exceptions.EmailDuplicadoException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // 👈 NUEVO

import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;

@WebFluxTest
@ContextConfiguration(classes = { RouterRest.class })   // solo el router
@Import(ApiErrorFilter.class)                            // filtro real
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    // 👇 reemplaza @MockBean por @MockitoBean
    @MockitoBean
    private UserHandler handler;

    @Test
    void saveUser_ok() {
        Mockito.when(handler.save(any()))
                .thenReturn(
                        org.springframework.web.reactive.function.server.ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("""
                                        {
                                          "success": true,
                                          "message": "Usuario creado correctamente",
                                          "data": { "id": 1, "email": "ana@example.com" },
                                          "errors": null,
                                          "path": "/api/v1/usuarios",
                                          "timestamp": "2025-08-25T00:00:00Z"
                                        }
                                        """)
                );

        webTestClient.post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "firstName": "Ana",
                          "lastName": "García",
                          "birthdate": "1990-05-10",
                          "address": "Calle 123",
                          "phoneNumber": "3001234567",
                          "email": "ana@example.com",
                          "baseSalary": 1200.50,
                          "identityDocument": "CC1",
                          "roleId": 1
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.id").isEqualTo(1)
                .jsonPath("$.data.email").isEqualTo("ana@example.com")
                .jsonPath("$.path").isEqualTo("/api/v1/usuarios");
    }

    @Test
    void saveUser_emailDuplicado_conflict() {
        Mockito.when(handler.save(any()))
                .thenReturn(Mono.error(new EmailDuplicadoException("ana@example.com")));

        webTestClient.post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        { "firstName":"Ana","lastName":"García","birthdate":"1990-05-10",
                          "address":"Calle 123","phoneNumber":"3001234567","email":"ana@example.com",
                          "baseSalary":1200.5,"identityDocument":"CC1","roleId":1 }
                        """)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.errors.email").isEqualTo("ana@example.com")
                .jsonPath("$.path").isEqualTo("/api/v1/usuarios");
    }

    @Test
    void saveUser_validationError_badRequest() {
        Mockito.when(handler.save(any()))
                .thenReturn(Mono.error(new ConstraintViolationException(java.util.Set.of())));

        webTestClient.post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        { "firstName":"Ana","lastName":"García","birthdate":"1990-05-10",
                          "address":"Calle 123","phoneNumber":"3001234567","email":"ana@example.com",
                          "baseSalary":1200.5,"identityDocument":"CC1","roleId":1 }
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Datos de entrada inválidos")
                .jsonPath("$.path").isEqualTo("/api/v1/usuarios");
    }
}

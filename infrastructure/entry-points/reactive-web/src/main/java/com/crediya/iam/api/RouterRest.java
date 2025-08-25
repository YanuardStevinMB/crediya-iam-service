package com.crediya.iam.api;

import com.crediya.iam.usecase.user.exceptions.*;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;


@Configuration
public class RouterRest {
    @Bean
    public RouterFunction<ServerResponse> routerFunction(UserHandler handler) {
        return route(POST("/api/v1/usuarios"), handler::save)
                .filter((req, next) -> next.handle(req)
                        // 409 cuando el email ya existe
                        .onErrorResume(EmailDuplicadoException.class, ex ->
                                ServerResponse.status(HttpStatus.CONFLICT)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(Map.of(
                                                "code", ex.getCode(),
                                                "message", ex.getMessage(),
                                                "email", ex.getEmail()
                                        ))
                        )
                        // si aún usas esta otra excepción, mantenla también:
                        .onErrorResume(UserAlreadyExistsException.class, ex ->
                                ServerResponse.status(HttpStatus.CONFLICT)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(Map.of(
                                                "code", ex.getCode(),
                                                "message", ex.getMessage(),
                                                "email", ex.getEmail()
                                        ))
                        )
                        .onErrorResume(ServiceException.class, ex ->
                                ServerResponse.status(HttpStatus.BAD_REQUEST)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(Map.of(
                                                "code", ex.getCode(),
                                                "message", ex.getMessage()
                                        ))
                        )
                        .onErrorResume(ConstraintViolationException.class, ex ->
                                ServerResponse.status(HttpStatus.BAD_REQUEST)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(Map.of(
                                                "code", "VALIDATION_ERROR",
                                                "message", "Datos de entrada inválidos",
                                                "details", ex.getConstraintViolations().stream()
                                                        .map(v -> Map.of(
                                                                "field", v.getPropertyPath().toString(),
                                                                "message", v.getMessage()
                                                        ))
                                                        .collect(Collectors.toList())
                                        ))
                        )
                        .onErrorResume(IllegalArgumentException.class, ex ->
                                ServerResponse.status(HttpStatus.BAD_REQUEST)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(Map.of(
                                                "code", "INVALID_ARGUMENT",
                                                "message", ex.getMessage()
                                        ))
                        )
                        // fallback final
                        .onErrorResume(Throwable.class, ex ->
                                ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(Map.of(
                                                "code", "INTERNAL_ERROR",
                                                "message", "Ocurrió un error inesperado"
                                        ))
                        )
                        // 404: rol no existe (tu mapeo directo)
                        .onErrorResume(RoleNotFoundException.class, ex ->
                                ServerResponse.status(HttpStatus.NOT_FOUND)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(Map.of(
                                                "code", ex.getCode(),
                                                "message", "El rol especificado no existe",
                                                "roleId", ex.getRoleId()
                                        ))
                        )

                        // 404/409: violación de FK detectada en infraestructura
                        .onErrorResume(ForeignKeyViolationException.class, ex -> {
                            // Si la columna de la FK es 'id_rol' o 'role_id', respondemos como "rol no existe"
                            String field = ex.getField() == null ? "" : ex.getField();
                            if ("id_rol".equalsIgnoreCase(field) || "role_id".equalsIgnoreCase(field)) {
                                return ServerResponse.status(HttpStatus.NOT_FOUND)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(Map.of(
                                                "code", "ROLE_NOT_FOUND",
                                                "message", "El rol especificado no existe",
                                                "field", field,
                                                "value", ex.getValue()
                                        ));
                            }
                            // Para cualquier otra FK, 409 con detalle de la columna
                            return ServerResponse.status(HttpStatus.CONFLICT)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(Map.of(
                                            "code", "FK_VIOLATION",
                                            "message", ex.getMessage(),
                                            "field", field,
                                            "value", ex.getValue()
                                    ));
                        })

                        // ... (deja aquí tus otros onErrorResume existentes: EmailDuplicadoException, SalaryValidateException, etc.)

                        // Safety net: desanidar (por si viene envuelta)
                        .onErrorResume(t -> {
                            Throwable e = reactor.core.Exceptions.unwrap(t);
                            if (e instanceof ForeignKeyViolationException ex) {
                                String field = ex.getField() == null ? "" : ex.getField();
                                if ("id_rol".equalsIgnoreCase(field) || "role_id".equalsIgnoreCase(field)) {
                                    return ServerResponse.status(HttpStatus.NOT_FOUND)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .bodyValue(Map.of(
                                                    "code", "ROLE_NOT_FOUND",
                                                    "message", "El rol especificado no existe",
                                                    "field", field,
                                                    "value", ex.getValue()
                                            ));
                                }
                                return ServerResponse.status(HttpStatus.CONFLICT)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(Map.of(
                                                "code", "FK_VIOLATION",
                                                "message", ex.getMessage(),
                                                "field", field,
                                                "value", ex.getValue()
                                        ));
                            }
                            return Mono.error(e); // deja que lo manejen los siguientes bloques o el fallback
                        })


                        // Fallback final (INTERNAL_ERROR) — déjalo al final del filtro
//                        .onErrorResume(Throwable.class, ex ->
//                                ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                                        .contentType(MediaType.APPLICATION_JSON)
//                                        .bodyValue(Map.of(
//                                                "code", "INTERNAL_ERROR",
//                                                "message", "Ocurrió un error inesperado"
//                                        ))
//                        )
                );

    }
}

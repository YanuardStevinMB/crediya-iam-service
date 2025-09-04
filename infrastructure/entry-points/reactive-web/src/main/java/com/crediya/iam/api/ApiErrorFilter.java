package com.crediya.iam.api;

import com.crediya.iam.usecase.user.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;


import java.util.Map;

import static com.crediya.iam.api.ApiErrorUtils.*;

@Component
public class ApiErrorFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    @Override
    public Mono<ServerResponse> filter(ServerRequest req, HandlerFunction<ServerResponse> next) {
        return next.handle(req)
                // === Específicas ===
                .onErrorResume(EmailDuplicadoException.class,
                        ex -> respond(req, HttpStatus.CONFLICT,
                                "Email duplicado",
                                Map.of("email", ex.getEmail(), "code", ex.getCode())))

                .onErrorResume(UserAlreadyExistsException.class,
                        ex -> respond(req, HttpStatus.CONFLICT,
                                "Email duplicado",
                                Map.of("email", ex.getEmail(), "code", ex.getCode())))

                .onErrorResume(RoleNotFoundException.class,
                        ex -> respond(req, HttpStatus.NOT_FOUND,
                                "El rol especificado no existe",
                                Map.of("roleId", ex.getRoleId(), "code", ex.getCode())))

                // === Validaciones y argumentos ===
                .onErrorResume(SalaryValidateException.class,
                        ex -> respond(req, HttpStatus.BAD_REQUEST, "Salario inválido", ex.getMessage()))

                .onErrorResume(ServiceException.class,
                        ex -> respond(req, HttpStatus.BAD_REQUEST, ex.getMessage(),
                                Map.of("code", ex.getCode())))


                .onErrorResume(IllegalArgumentException.class,
                        ex -> respond(req, HttpStatus.BAD_REQUEST, "Solicitud inválida", ex.getMessage()))

                // === Unwrap de Reactor ===
                .onErrorResume(t -> {
                    Throwable e = Exceptions.unwrap(t);
                     if (e instanceof EmailDuplicadoException dupe) {
                        return respond(req, HttpStatus.CONFLICT,
                                "Email duplicado", Map.of("email", dupe.getEmail(), "code", dupe.getCode()));
                    } else if (e instanceof RoleNotFoundException rnfe) {
                        return respond(req, HttpStatus.NOT_FOUND,
                                "El rol especificado no existe", Map.of("roleId", rnfe.getRoleId(), "code", rnfe.getCode()));
                    } else if (e instanceof SalaryValidateException sve) {
                        return respond(req, HttpStatus.BAD_REQUEST, "Salario inválido", sve.getMessage());
                    } else if (e instanceof IllegalArgumentException iae) {
                        return respond(req, HttpStatus.BAD_REQUEST, "Solicitud inválida", iae.getMessage());
                    }
                    return Mono.error(e); // pasa al catch-all
                })

                // === Catch-all ===
                .onErrorResume(ex -> respond(req, HttpStatus.INTERNAL_SERVER_ERROR,
                        "Ocurrió un error inesperado", null))
                .switchIfEmpty(ServerResponse.noContent().build());
    }
}

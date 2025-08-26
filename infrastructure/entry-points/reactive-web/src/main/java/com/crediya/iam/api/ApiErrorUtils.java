package com.crediya.iam.api;


import com.crediya.iam.api.dto.ApiResponse;
import com.crediya.iam.usecase.user.exceptions.ForeignKeyViolationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.stream.Collectors;

final class ApiErrorUtils {

    private ApiErrorUtils() { }

    static Mono<ServerResponse> respond(ServerRequest req,
                                        HttpStatus status,
                                        String message,
                                        Object errors) {
        var body = ApiResponse.fail(message, errors, req.path());
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body);
    }

    static Mono<ServerResponse> handleFk(ServerRequest req, ForeignKeyViolationException ex) {
        String field = ex.getField() == null ? "" : ex.getField();
        boolean isRole = "id_rol".equalsIgnoreCase(field) || "role_id".equalsIgnoreCase(field);
        if (isRole) {
            return respond(req, HttpStatus.NOT_FOUND,
                    "El rol especificado no existe",
                    Map.of("field", field, "value", ex.getValue(), "code", "ROLE_NOT_FOUND"));
        }
        return respond(req, HttpStatus.CONFLICT,
                "Violación de llave foránea",
                Map.of("field", field, "value", ex.getValue(), "code", "FK_VIOLATION"));
    }

    static Object violationsToList(ConstraintViolationException ex) {
        return ex.getConstraintViolations().stream()
                .map(v -> Map.of(
                        "field", v.getPropertyPath().toString(),
                        "message", v.getMessage(),
                        "invalidValue", safeValue(v)
                ))
                .collect(Collectors.toList());
    }

    private static Object safeValue(ConstraintViolation<?> v) {
        try {
            return v.getInvalidValue();
        } catch (Exception e) {
            return null;
        }
    }
}

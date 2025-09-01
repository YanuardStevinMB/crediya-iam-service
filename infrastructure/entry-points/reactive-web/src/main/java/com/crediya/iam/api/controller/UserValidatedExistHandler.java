package com.crediya.iam.api.controller;

import com.crediya.iam.api.dto.ApiResponse;
import com.crediya.iam.api.dto.UserExistRequestDto;
import com.crediya.iam.usecase.existuser.ExistUserUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserValidatedExistHandler {

    private final ExistUserUseCase existUserUseCase;

    public Mono<ServerResponse> loadExistUser(ServerRequest request) {
        final String path = request.path();
        final String method = request.methodName();

        return request.bodyToMono(UserExistRequestDto.class)
                .flatMap(req -> {
                    String document = req.getDocument();
                    String email = req.getEmail();
                    log.info("[{}] {} -> Checking user existence for document={}", method, path, document);

                    return existUserUseCase.execute(document, email)
                            .flatMap(exists -> {
                                if (Boolean.TRUE.equals(exists)) {
                                    return ServerResponse.ok()
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .bodyValue(ApiResponse.ok(true, "User already exists", path));
                                } else {
                                    return ServerResponse.ok()
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .bodyValue(ApiResponse.ok(false, "User does not exist", path));
                                }
                            });
                })
                .switchIfEmpty(ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.ok(false, "User does not exist", path)))
                // Manejo de errores específicos
                .onErrorResume(IllegalArgumentException.class, ex -> {
                    log.warn("[{}] Error de validación: {}", path, ex.getMessage());
                    return ServerResponse.badRequest()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.fail("Validación fallida", ex.getMessage(), path));
                })
                // Manejo de errores inesperados
                .onErrorResume(Throwable.class, ex -> {
                    log.error("[{}] Unexpected error checking user existence", path, ex);
                    return ServerResponse.status(500)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.fail("Internal Server Error", ex.getMessage(), path));
                });
    }
}

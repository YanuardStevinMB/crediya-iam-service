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
        String path = request.path();
        String method = request.methodName();

        return request.bodyToMono(UserExistRequestDto.class)
                .flatMap(req -> {
                    log.info("[{}] {} -> Checking user existence for document={}", method, path, req.getDocument());
                    return existUserUseCase.execute(req.getDocument(), req.getEmail())
                            .flatMap(exists -> buildOkResponse(exists, path));
                })
                .switchIfEmpty(buildOkResponse(false, path))
                .onErrorResume(IllegalArgumentException.class,
                        ex -> buildErrorResponse(400, "Validación fallida", ex.getMessage(), path))
                .onErrorResume(Throwable.class,
                        ex -> buildErrorResponse(500, "Internal Server Error", ex.getMessage(), path));
    }

    // ----------------- Métodos auxiliares -----------------

    private Mono<ServerResponse> buildOkResponse(boolean exists, String path) {
        String message = exists ? "User already exists" : "User does not exist";
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ApiResponse.ok(exists, message, path));
    }

    private Mono<ServerResponse> buildErrorResponse(int status, String error, String detail, String path) {
        if (status == 400) {
            log.warn("[{}] {}", path, detail);
        } else {
            log.error("[{}] {}", path, detail);
        }
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ApiResponse.fail(error, detail, path));
    }
}

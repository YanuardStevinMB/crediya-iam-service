package com.crediya.iam.api.controller;

import com.crediya.iam.api.dto.ApiResponse;
import com.crediya.iam.api.dto.UserResponseDto;
import com.crediya.iam.api.dto.UserSaveDto;
import com.crediya.iam.api.userMapper.UserMapper;
import com.crediya.iam.model.user.User;
import com.crediya.iam.usecase.loadusers.LoadUsersUseCase;
import com.crediya.iam.usecase.shared.Messages;
import com.crediya.iam.usecase.user.IUserUseCase;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserHandler {

    private final IUserUseCase useCase;
    private  final LoadUsersUseCase loadUsersUseCase;
    private final UserMapper mapper;
    private final Validator validator;

    private <T> Mono<T> validate(T body) {
        var violations = validator.validate(body);
        if (!violations.isEmpty()) {
            return Mono.error(new ConstraintViolationException(violations));
        }
        return Mono.just(body);
    }


    public Mono<ServerResponse> list(ServerRequest request) {
        final String path = request.path();

        return loadUsersUseCase.execute()
                .collectList()
                .flatMap(list -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.ok(list, Messages.USERS_FOUND, path)));

    }



    public Mono<ServerResponse> save(ServerRequest request) {
        final String path = request.path();
        final String method = request.methodName();

        log.info("[{}] {} -> Inicio creación de usuario", method, path);

        return request.bodyToMono(UserSaveDto.class)
                .doOnNext(dto -> log.debug("[{}] Payload recibido: email={}, doc={}, roleId={}",
                        path, dto.email(), mask(dto.identityDocument()), dto.roleId()))
                .flatMap(this::validate)
                .doOnNext(dto -> log.debug("[{}] Validación OK para email={}", path, dto.email()))
                .map(mapper::toModel)
                .doOnNext(model -> log.debug("[{}] Mapeo a modelo OK: {}", path, model))
                .flatMap(useCase::execute)
                .doOnNext(user -> log.info("[{}] UseCase ejecutado. userId={}", path, user.getId()))
                .map(mapper::toResponseDto)
                .flatMap((UserResponseDto dto) -> {
                    log.info("[{}] Usuario creado con éxito. id={}", path, dto.getId());
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.ok(dto, Messages.USER_SAVED, path));
                });

    }

    private static String mask(String doc) {
        if (doc == null || doc.length() < 4) return "****";
        int visible = Math.min(2, doc.length() / 2);
        return doc.substring(0, visible) + "****" + doc.substring(doc.length() - visible);
    }



}

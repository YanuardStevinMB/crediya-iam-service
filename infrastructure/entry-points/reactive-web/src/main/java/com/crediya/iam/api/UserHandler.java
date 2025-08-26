package com.crediya.iam.api;

import com.crediya.iam.api.dto.ApiResponse;
import com.crediya.iam.api.dto.UserResponseDto;
import com.crediya.iam.api.dto.UserSaveDto;
import com.crediya.iam.api.userMapper.UserMapper;
import com.crediya.iam.usecase.user.IUserUseCase;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserHandler {

    private final IUserUseCase useCase;
    private final UserMapper mapper;
    private final Validator validator;

    private <T> Mono<T> validate(T body) {
        var violations = validator.validate(body);
        if (!violations.isEmpty()) {
            return Mono.error(new ConstraintViolationException(violations));
        }
        return Mono.just(body);
    }

    public Mono<ServerResponse> save(ServerRequest request) {
        final String path = request.path();

        return request.bodyToMono(UserSaveDto.class)
                .flatMap(this::validate)
                .map(mapper::toModel)
                .flatMap(useCase::execute)
                .map(mapper::toResponseDto)
                .flatMap((UserResponseDto dto) -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.ok(dto, "Usuario creado correctamente", path))
                );

    }
}

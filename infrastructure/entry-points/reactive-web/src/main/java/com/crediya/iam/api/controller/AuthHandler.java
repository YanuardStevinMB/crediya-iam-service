package com.crediya.iam.api.controller;

import com.crediya.iam.api.dto.*;
import com.crediya.iam.api.userMapper.UserMapper;
import com.crediya.iam.usecase.authenticate.AuthenticateUseCase;
import jakarta.validation.Validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthHandler {

    private final AuthenticateUseCase authenticate;
    private final UserMapper mapper;
    private final Validator validator;


    public Mono<ServerResponse> login(ServerRequest req) {
        return req.bodyToMono(LoginRequestDto.class)
                .flatMap(body -> authenticate.login(body.email(), body.password()))
                .flatMap(tok -> ServerResponse.ok().bodyValue(
                        new LoginResponseDto(tok.token(), tok.tokenType(), tok.expiresAtEpochSec())))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.status(401).bodyValue(new ErrorDto("invalid_credentials")))
                .onErrorResume(IllegalStateException.class,
                        e -> ServerResponse.status(403).bodyValue(new ErrorDto("inactive_user")));
    }

}

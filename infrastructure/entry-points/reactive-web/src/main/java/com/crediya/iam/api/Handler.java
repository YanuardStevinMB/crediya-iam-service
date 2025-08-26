package com.crediya.iam.api;

import com.crediya.iam.model.user.User;
import com.crediya.iam.usecase.user.IUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class Handler {


private  final IUserUseCase useCase;
//private  final UseCase2 useCase2;

    public Mono<ServerResponse> listenPOSTUseCase(ServerRequest serverRequest) {
        return serverRequest
                .bodyToMono(User.class)
                .flatMap(useCase::execute) // ✅ aquí se pasa la referencia al método
                .flatMap(userSaved -> ServerResponse.ok().bodyValue(userSaved));
    }


}

package com.crediya.iam.usecase.loadusers;

import com.crediya.iam.model.user.User;
import com.crediya.iam.model.user.gateways.UserRepository;
import com.crediya.iam.usecase.existuser.ExistUserUseCase;
import com.crediya.iam.usecase.shared.Messages;
import com.crediya.iam.usecase.shared.ValidationException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

@RequiredArgsConstructor
public class LoadUsersUseCase {
    private static final Logger LOG = Logger.getLogger(LoadUsersUseCase.class.getName());
    private final UserRepository userRepository;

    public Flux<User> execute() {
        return userRepository.findAll()
                .flatMap(user -> {
                    return Flux.just(user);
                })
                .switchIfEmpty(Mono.error(new ValidationException("User",Messages.USERS_NOT_FOUND)));
    }
}

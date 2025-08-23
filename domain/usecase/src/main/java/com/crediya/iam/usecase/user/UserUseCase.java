package com.crediya.iam.usecase.user;

import com.crediya.iam.model.user.User;
import com.crediya.iam.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UserUseCase {
    final UserRepository userReactiveRepository;

    public Mono<User> save(User user) {
        return userReactiveRepository.save(user);
    }
}

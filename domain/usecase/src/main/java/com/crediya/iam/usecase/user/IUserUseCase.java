package com.crediya.iam.usecase.user;

import com.crediya.iam.model.user.User;
import reactor.core.publisher.Mono;


public interface IUserUseCase {

    Mono<User> execute(User user);


}

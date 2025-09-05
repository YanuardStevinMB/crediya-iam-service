package com.crediya.iam.usecase.authenticate;

import com.crediya.iam.model.user.User;
import reactor.core.publisher.Mono;

public interface TokenGeneratorPort {
    Mono<TokenResult> generate(User user, String role);
}
package com.crediya.iam.security.security;

import com.crediya.iam.usecase.shared.security.PasswordService;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import com.crediya.iam.usecase.shared.security.PasswordService;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class ReactivePasswordManager implements PasswordService {

    private static final int COST = 12; // coste recomendado 10-14

    @Override
    public Mono<String> generatePasswordHash(String plainPassword) {
        return Mono.fromCallable(() -> {
                    if (plainPassword == null || plainPassword.isBlank()) {
                        throw new IllegalArgumentException("La contraseña no puede ser nula o vacía");
                    }
                    return BCrypt.hashpw(plainPassword, BCrypt.gensalt(COST));
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Boolean> matches(String raw, String hash) {
        return Mono.fromCallable(() -> BCrypt.checkpw(raw, hash))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
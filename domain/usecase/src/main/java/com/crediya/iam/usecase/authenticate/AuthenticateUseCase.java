package com.crediya.iam.usecase.authenticate;

import com.crediya.iam.model.user.gateways.UserRepository;
import reactor.core.publisher.Mono;

import java.util.Objects;

public class AuthenticateUseCase  {

    private final UserRepository users;
    private final PasswordHasherPort passwordHasher;
    private final TokenGeneratorPort tokens;

    public AuthenticateUseCase(
            UserRepository users,
            PasswordHasherPort passwordHasher,
            TokenGeneratorPort tokens
    ) {
        this.users = Objects.requireNonNull(users);
        this.passwordHasher = Objects.requireNonNull(passwordHasher);
        this.tokens = Objects.requireNonNull(tokens);
    }

    public Mono<TokenResult> login(String email, String rawPassword) {
        if (email == null || email.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            return Mono.error(new IllegalArgumentException("Credenciales inválidas"));
        }
        return users.findByEmail(email.trim().toLowerCase())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Credenciales inválidas")))
                .flatMap(u -> {
                    if (Boolean.FALSE.equals(u.getActive())) {
                        return Mono.error(new IllegalStateException("Usuario inactivo"));
                    }
                    if (!passwordHasher.matches(rawPassword, u.getPassword())) {
                        return Mono.error(new IllegalArgumentException("Credenciales inválidas"));
                    }
                    return tokens.generate(u);
                });
    }
}

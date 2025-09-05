package com.crediya.iam.usecase.shared.security;

import reactor.core.publisher.Mono;

public interface PasswordService  {
    /**
     * Genera una contraseña aleatoria en texto plano,
     * la hashea con BCrypt y retorna el hash en un Mono.
     */
    Mono<String> generatePasswordHash(String plainPassword);

    Mono<Boolean> matches(String raw, String hash);

}
package com.crediya.iam.usecase.user;

import com.crediya.iam.model.user.User;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * Caso de uso principal para la gestión de usuarios.
 */
public interface IUserUseCase {

    /**
     * Guarda un usuario en el sistema.
     *
     * @param user modelo de dominio User
     * @return Mono con el DTO del usuario guardado
     */
    Mono<User> execute(User user);


}

package com.crediya.iam.model.user.gateways;

import com.crediya.iam.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * UserRepository define las operaciones de acceso a datos (persistencia)
 * para la entidad {@link User}.
 *
 * Se implementa bajo un enfoque reactivo usando Project Reactor (Mono/Flux),
 * lo que permite trabajar de forma no bloqueante.
 */
public interface UserRepository {

    /**
     * Busca un usuario por su identificador único.
     *
     * @param id Identificador UUID del usuario.
     * @return Mono con el usuario encontrado o vacío si no existe.
     */
    Mono<User> findById(UUID id);

    /**
     * Busca un usuario por su nombre.
     *
     * @param name Nombre del usuario.
     * @return Mono con el usuario encontrado o vacío si no existe.
     */
    Mono<User> findByName(String name);

    /**
     * Busca un usuario por su correo electrónico.
     *
     * @param email Correo electrónico del usuario.
     * @return Mono con el usuario encontrado o vacío si no existe.
     */
    Mono<User> findByEmail(String email);

    /**
     * Recupera todos los usuarios registrados.
     *
     * @return Flux con la lista de usuarios.
     */
    Flux<User> findAll();

    /**
     * Registra un nuevo usuario en la base de datos.
     *
     * @param user Usuario a guardar.
     * @return Mono con el usuario guardado.
     */
    Mono<User> save(User user);

    /**
     * Actualiza la información de un usuario existente.
     *
     * @param user Usuario con la información actualizada.
     * @return Mono con el usuario actualizado.
     */
    Mono<User> update(User user);

    /**
     * Elimina un usuario de la base de datos por su identificador.
     *
     * @param id Identificador UUID del usuario.
     * @return Mono vacío al completar la operación.
     */
    Mono<Void> deleteById(UUID id);
}

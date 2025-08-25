package com.crediya.iam.r2dbc.userRepository;

import com.crediya.iam.model.user.User;
import com.crediya.iam.model.user.gateways.UserRepository;
import com.crediya.iam.r2dbc.entity.UserEntity;
import com.crediya.iam.r2dbc.helper.ReactiveAdapterOperations;
import com.crediya.iam.r2dbc.mapper.UserEntityMapper;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Slf4j

/**
 * Implementación del repositorio reactivo de usuarios.
 *
 * Esta clase actúa como un adaptador entre la capa de dominio (modelo User)
 * y la capa de persistencia (entidad UserEntity con R2DBC).
 *
 * Extiende de {@link ReactiveAdapterOperations}, lo que permite reutilizar
 * operaciones genéricas de persistencia reactiva, y además implementa
 * la interfaz {@link UserRepository} definida en el dominio.
 */
@Repository
public class UserReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        User,       // Modelo de dominio
        UserEntity, // Modelo de persistencia (entidad)
        Long,       // Tipo de dato de la clave primaria
        UserReactiveRepository // Repositorio reactivo de Spring Data R2DBC
        > implements UserRepository {

    private final UserEntityMapper userEntityMapper;

    /**
     * Constructor del adaptador.
     *
     * @param repository repositorio reactivo de R2DBC
     * @param userEntityMapper mapper para convertir entre User y UserEntity
     * @param mapper utilitario de mapeo genérico de Reactive Commons
     */
    public UserReactiveRepositoryAdapter(UserReactiveRepository repository,
                                         UserEntityMapper userEntityMapper,
                                         ObjectMapper mapper) {
        // Se configura el adaptador para transformar UserEntity -> User
        super(repository, mapper, entity -> mapper.map(entity, User.class));
        this.userEntityMapper = userEntityMapper;
    }

    /**
     * Verifica si existe un usuario en la base de datos con el correo indicado.
     *
     * @param mail correo electrónico a validar
     * @return Mono con true si el usuario existe, false en caso contrario
     */
    @Override
    public Mono<Boolean> existsByMail(String mail) {
        if (mail == null) return Mono.just(false);
        return repository.existsByEmail(mail.trim());
    }

    /**
     * Persiste un usuario en la base de datos.
     *
     * Convierte el modelo de dominio {@link User} a la entidad {@link UserEntity},
     * lo guarda en la base de datos y luego vuelve a convertirlo a dominio.
     *
     * @param user usuario en modelo de dominio
     * @return Mono con el usuario guardado (en modelo de dominio)
     */
    @Override
    public Mono<User> save(User user) {
        System.out.println(user.getPhoneNumber());
        var entity = userEntityMapper.toEntity(user);
        return repository.save(entity)
                .map(userEntityMapper::toDomain)
                .onErrorMap(t -> {

                    return t; // no reconocido
                });
    }
}

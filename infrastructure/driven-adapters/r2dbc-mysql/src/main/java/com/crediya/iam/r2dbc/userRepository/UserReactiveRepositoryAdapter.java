package com.crediya.iam.r2dbc.userRepository;

import com.crediya.iam.model.user.User;
import com.crediya.iam.model.user.gateways.UserRepository;
import com.crediya.iam.r2dbc.entity.UserEntity;
import com.crediya.iam.r2dbc.helper.ReactiveAdapterOperations;
import com.crediya.iam.r2dbc.mapper.UserEntityMapper;
import com.crediya.iam.r2dbc.roleRepository.RoleReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Locale;


@Slf4j
@Repository
public class UserReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        User,
        UserEntity,
        Long,
        UserReactiveRepository
        > implements UserRepository {

    private final UserEntityMapper userEntityMapper;
    private final RoleReactiveRepository roleRepository;

    public UserReactiveRepositoryAdapter(UserReactiveRepository repository,
                                         UserEntityMapper userEntityMapper,
                                         ObjectMapper mapper,
                                         RoleReactiveRepository roleRepository) {
        super(repository, mapper, entity -> mapper.map(entity, User.class));
        this.userEntityMapper = userEntityMapper;
        this.roleRepository = roleRepository;
    }

    @Override
    public Mono<Boolean> existsByMail(String mail) {
        if (mail == null) return Mono.just(false);
        return repository.existsByEmail(mail.trim().toLowerCase(Locale.ROOT));
    }

    public Mono<User> existUserForDocument(String document) {
        if (document == null) return Mono.empty();

        return repository.existUserForDocument(document)
                .doOnSubscribe(s -> log.info("[user.existUserForDocument] executing query for document={}", document))
                .doOnNext(user -> log.info("[user.existUserForDocument] found=true id={} document={} email={}",
                        user.getId(), user.getIdentityDocument(), user.getEmail()))
                .doOnSuccess(user -> {
                    if (user == null) {
                        log.info("[user.existUserForDocument] found=false document={}", document);
                    }
                })
                .map(userEntityMapper::toDomain)
                .doOnSuccess(user -> {
                    if (user != null) {
                        log.info("[user.existUserForDocument] mapped to domain: id={} email={}", user.getId(), user.getEmail());
                    }
                });
    }


    @Override
    public Mono<User> findByEmail(String mail) {
        if(mail== null) return Mono.empty();
        return repository.findByEmail(mail.trim().toLowerCase(Locale.ROOT));

    }

    @Override
    public Mono<User> save(User user) {
        // 1) Validar que el roleId venga y exista en BD
        Long roleId = user.getRoleId();
        if (roleId == null) {
            return Mono.error(new IllegalArgumentException("El roleId es obligatorio"));
        }

        return roleRepository.existsById(roleId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new IllegalArgumentException("El roleId no existe: " + roleId));
                    }
                    // 2) Mapear y guardar si el rol existe
                    var entity = userEntityMapper.toEntity(user);
                    return repository.save(entity);
                })

                .map(userEntityMapper::toDomain)
                .doOnSuccess(saved -> log.info("[user.save] id={} email={} roleId={}",
                        saved.getId(), saved.getEmail(), saved.getRoleId()))
                .doOnError(err -> log.warn("[user.save] failed: {}", err.toString()));
    }
}

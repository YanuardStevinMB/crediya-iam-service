package com.crediya.iam.r2dbc.userRepository;

import com.crediya.iam.model.user.User;
import com.crediya.iam.r2dbc.entity.UserEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserReactiveRepository extends ReactiveCrudRepository<UserEntity, Long>, ReactiveQueryByExampleExecutor<UserEntity> {

    Mono<Boolean> existsByEmail(String email);
    Mono<User> findByEmail(String email);
        @Query("SELECT * FROM Usuario WHERE documento_identidad = :document")
        Mono<UserEntity> existUserForDocument(@Param("document") String document);

}
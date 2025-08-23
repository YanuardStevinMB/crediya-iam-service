package com.crediya.iam.r2dbc.userRepository;

import com.crediya.iam.model.user.User;
import com.crediya.iam.model.user.gateways.UserRepository;
import com.crediya.iam.r2dbc.MyReactiveRepository;
import com.crediya.iam.r2dbc.entity.UserEntity;
import com.crediya.iam.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.reactivestreams.Publisher;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.swing.text.html.parser.Entity;
import java.util.UUID;
import java.util.function.Function;
@Repository
public class UserReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        User/* change for domain model */,
        UserEntity/* change for adapter model */,
        Long,
        UserReactiveRepository
        > implements UserRepository {

    public UserReactiveRepositoryAdapter(UserReactiveRepository repository, ObjectMapper mapper) {
        // Mapea UserEntity (DB) -> User (dominio)
        super(repository, mapper, entity -> mapper.map(entity, User.class));
    }

    @Override
    public Mono<User> findById(UUID id) {
        return null;
    }

    @Override
    public Mono<User> findByName(String name) {
        return null;
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return null;
    }

    @Override
    public Mono<User> save(User entity) {
        return super.save(entity);
    }

    @Override
    public Mono<User> update(User user) {
        return null;
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return null;
    }

}

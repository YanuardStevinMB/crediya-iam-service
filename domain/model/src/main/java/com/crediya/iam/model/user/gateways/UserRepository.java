package com.crediya.iam.model.user.gateways;

import com.crediya.iam.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRepository {
    /**
     * Checks whether a user already exists with the given mail.
     *
     * @param mail the email to check
     * @return Mono emitting true if the user exists, false otherwise
     */
    Mono<Boolean> existsByMail(String mail);

    Mono<User> existUserForDocument(String document);

    Mono<User> findByEmail(String mail);
    Flux<User> findAll();

    /**
     * Persists a user in the repository.
     *
     * @param user the user to save
     * @return Mono emitting the saved user
     */
    Mono<User> save(User user);
}

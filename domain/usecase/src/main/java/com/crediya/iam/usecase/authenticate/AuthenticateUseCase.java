package com.crediya.iam.usecase.authenticate;

import com.crediya.iam.model.role.gateways.RoleRepository;
import com.crediya.iam.model.user.gateways.UserRepository;
import com.crediya.iam.usecase.shared.Messages;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.logging.Logger;

public class AuthenticateUseCase {

    private final UserRepository users;
    private final PasswordHasherPort passwordHasher;
    private final RoleRepository roleRepository;
    private final TokenGeneratorPort tokens;
    private static final Logger LOG = Logger.getLogger(AuthenticateUseCase.class.getName());

    public AuthenticateUseCase(
            UserRepository users,
            PasswordHasherPort passwordHasher,
            TokenGeneratorPort tokens,
            RoleRepository roleRepository
    ) {
        this.users = Objects.requireNonNull(users);
        this.passwordHasher = Objects.requireNonNull(passwordHasher);
        this.tokens = Objects.requireNonNull(tokens);
        this.roleRepository = Objects.requireNonNull(roleRepository);
    }

    public Mono<TokenResult> login(String email, String rawPassword) {
        if (email == null || email.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            return Mono.error(new IllegalArgumentException(Messages.INVALID_CREDENTIALS));
        }

        return users.findByEmail(email.trim().toLowerCase())
                .switchIfEmpty(Mono.error(new IllegalArgumentException(Messages.INVALID_CREDENTIALS)))
                .flatMap(u -> {
                    if (!passwordHasher.matches(rawPassword, u.getPassword())) {
                        return Mono.error(new IllegalArgumentException(Messages.INVALID_CREDENTIALS));
                    }

                    // Obtenemos el rol y generamos token
                    return roleRepository.findById(u.getRoleId())
                            .switchIfEmpty(Mono.error(new IllegalArgumentException(Messages.ROLE_NOT_FOUND)))
                            .doOnNext(role -> LOG.info(
                                    String.format("[AuthenticateUseCase] UsuarioId=%s Email=%s Rol=%s",
                                            u.getId(), u.getEmail(), role.getName())
                            ))


                            .flatMap(role -> tokens.generate(u, role.getName()));
                });
    }
}

package com.crediya.iam.usecase.user;

import com.crediya.iam.model.user.User;
import com.crediya.iam.model.user.gateways.UserRepository;
import com.crediya.iam.usecase.shared.security.PasswordService;
import com.crediya.iam.usecase.user.exceptions.EmailDuplicadoException;
import com.crediya.iam.usecase.user.generaterequest.UserValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

@RequiredArgsConstructor
public class CreateUserUseCase implements IUserUseCase {

    private static final Logger LOG = Logger.getLogger(CreateUserUseCase.class.getName());

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    @Override
    public Mono<User> execute(User u) {
        return Mono.defer(() -> {
            // 1) Validación rápida
            UserValidator.validateAndNormalize(u);
            LOG.fine(() -> "Intento de creación de usuario con email=" + u.getEmail());

            // 2) Verificar duplicados
            return userRepository.existsByMail(u.getEmail())
                    .flatMap(exists -> {
                        if (Boolean.TRUE.equals(exists)) {
                            LOG.info(() -> "Creación abortada: email duplicado=" + u.getEmail());
                            return Mono.error(new EmailDuplicadoException(u.getEmail()));
                        }
                        return Mono.just(u);
                    })
                    // 3) Generar hash de la contraseña
                    .flatMap(user ->
                            passwordService.generatePasswordHash(user.getPassword())
                                    .map(hash -> {
                                        user.setPassword(hash);
                                        return user;
                                    })
                    )
                    // 4) Guardar en repositorio
                    .flatMap(userRepository::save)
                    .doOnSuccess(saved ->
                            LOG.info(() -> "Usuario creado id=" + saved.getId() + " email=" + saved.getEmail()));
        }).doOnError(e ->
                LOG.warning(() -> "CreateUserUseCase.execute() terminó con error: " + e.getMessage())
        );
    }
}

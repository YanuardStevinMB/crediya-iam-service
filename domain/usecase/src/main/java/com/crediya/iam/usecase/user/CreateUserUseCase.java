package com.crediya.iam.usecase.user;

import com.crediya.iam.model.user.User;
import com.crediya.iam.model.user.gateways.UserRepository;
import com.crediya.iam.usecase.shared.security.PasswordService;
import com.crediya.iam.usecase.user.exceptions.EmailDuplicadoException;
import com.crediya.iam.usecase.user.exceptions.SalaryValidateException;
import com.crediya.iam.usecase.user.gateway.TransactionGateway;
import com.crediya.iam.usecase.user.generaterequest.UserValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class CreateUserUseCase implements IUserUseCase {

    private static final Logger LOG = Logger.getLogger(CreateUserUseCase.class.getName());

    private final TransactionGateway tx;
    private final UserRepository userRepository;
    private  final PasswordService passwordService;


    @Override
    public Mono<User> execute(User u) {
        return Mono.defer(() -> {
            // 1) Validación sincrónica (rápida) OK en el hilo actual
            UserValidator.validateAndNormalize(u);
            LOG.fine(() -> "Intento de creación de usuario con email=" + u.getEmail());

            // 2) Transacción reactiva
            return tx.required(() ->
                    userRepository.existsByMail(u.getEmail())
                            .flatMap(exists -> {
                                if (Boolean.TRUE.equals(exists)) {
                                    LOG.info(() -> "Creación abortada: email duplicado=" + u.getEmail());
                                    return Mono.error(new EmailDuplicadoException(u.getEmail()));
                                }
                                return Mono.just(u);
                            })
                            // 3) Hash de contraseña (reactivo + offload)
                            .flatMap(user ->
                                    passwordService.generatePasswordHash(user.getPassword())
                                            .map(hash -> {
                                                user.setPassword(hash);
                                                return user;
                                            })
                            )
                            // 4) Persistir
                            .flatMap(userRepository::save)
                            .doOnSuccess(saved ->
                                    LOG.info(() -> "Usuario creado id=" + saved.getId() + " email=" + saved.getEmail()))
            );
        }).doOnError(e ->
                LOG.warning(() -> "CreateUserUseCase.execute() terminó con error: " + e.getMessage())
        );
    }
}
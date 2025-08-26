package com.crediya.iam.usecase.user;

import com.crediya.iam.model.user.User;
import com.crediya.iam.model.user.gateways.UserRepository;
import com.crediya.iam.usecase.user.exceptions.EmailDuplicadoException;
import com.crediya.iam.usecase.user.exceptions.SalaryValidateException;
import com.crediya.iam.usecase.user.gateway.TransactionGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class CreateUserUseCase implements IUserUseCase{

    private static final Logger LOG = Logger.getLogger(CreateUserUseCase.class.getName());

    private final TransactionGateway tx;
    private final UserRepository userRepository;

    private static final BigDecimal SALARIO_MIN = new BigDecimal("0.00");
    private static final BigDecimal SALARIO_MAX = new BigDecimal("15000000.00");
    private static final Pattern EMAIL_RE =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public Mono<User> execute(User u) {
        return Mono.defer(() -> {

            if (u == null) {
                LOG.warning("CreateUserUseCase.execute() recibió un User nulo");
                return Mono.error(new IllegalArgumentException("El usuario es obligatorio"));
            }
            //Numero de documento validado
            if (u.getIdentityDocument() == null) {
                LOG.warning("CreateUserUseCase.execute() recibió un User con documento nulo");
                return Mono.error(new IllegalArgumentException("El documento de identidad es obligatorio"));
            }
            // Número de teléfono validación
            if (u.getPhoneNumber() == null || u.getPhoneNumber().isBlank()) {
                LOG.warning("CreateUserUseCase.execute() recibió un User con teléfono nulo o vacío");
                return Mono.error(new IllegalArgumentException("El número de teléfono es obligatorio"));
            }

            if (!u.getPhoneNumber().matches("\\d+")) {
                LOG.warning("CreateUserUseCase.execute() recibió un teléfono no numérico");
                return Mono.error(new IllegalArgumentException("El número de teléfono debe contener solo dígitos"));
            }


            // Email obligatorio + normalización
            final String rawEmail = u.getEmail();
            if (rawEmail == null || rawEmail.isBlank()) {
                LOG.warning("Validación fallida: email vacío");
                return Mono.error(new IllegalArgumentException("El correo_electronico es obligatorio"));
            }
            final String emailNorm = rawEmail.trim().toLowerCase(Locale.ROOT);
            u.setEmail(emailNorm);

            // Validación salario
            final BigDecimal s = u.getBaseSalary();
            var salaryError = validateSalary(s);
            if (salaryError != null) {
                LOG.warning(() -> "Validación de salario falló: " + salaryError.getMessage());
                return Mono.error(salaryError);
            }

            // Formato email
            if (!EMAIL_RE.matcher(emailNorm).matches()) {
                LOG.warning(() -> "Formato de email inválido: " + emailNorm);
                return Mono.error(new IllegalArgumentException("Correo_electronico inválido"));
            }

            LOG.fine(() -> "Intento de creación de usuario con email=" + emailNorm);

            // Bloque transaccional (R2DBC, no bloqueante)
            return tx.required(() ->
                    userRepository.existsByMail(emailNorm)
                            .flatMap(exists -> {
                                if (Boolean.TRUE.equals(exists)) {
                                    LOG.info(() -> "Creación abortada: email duplicado=" + emailNorm);
                                    return Mono.error(new EmailDuplicadoException(emailNorm));
                                }
                                return userRepository.save(u)
                                        .doOnSuccess(saved ->
                                                LOG.info(() -> "Usuario creado id=" + saved.getId()
                                                        + " email=" + saved.getEmail()));
                            })
            );
        }).doOnError(e ->
                LOG.warning(() -> "CreateUserUseCase.execute() terminó con error: " + e.getMessage())
        );
    }

    private static SalaryValidateException validateSalary(BigDecimal s) {
        if (s == null) {
            return new SalaryValidateException(
                    "El salario debe ser un valor numérico entre 0 y 15,000,000");
        }
        if (s.scale() > 2) {
            return new SalaryValidateException("El salario admite máximo 2 decimales");
        }
        if (s.compareTo(SALARIO_MIN) < 0 || s.compareTo(SALARIO_MAX) > 0) {
            return new SalaryValidateException("El salario debe estar entre 0 y 15,000,000");
        }
        return null;
    }
}

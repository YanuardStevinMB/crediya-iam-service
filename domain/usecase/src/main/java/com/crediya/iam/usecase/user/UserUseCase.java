package com.crediya.iam.usecase.user;

import com.crediya.iam.model.user.User;
import com.crediya.iam.model.user.gateways.UserRepository;
import com.crediya.iam.usecase.user.exceptions.EmailDuplicadoException;
import com.crediya.iam.usecase.user.exceptions.SalaryValidateException;
import com.crediya.iam.usecase.user.exceptions.UniqueConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class UserUseCase implements IUserUseCase {
    private static final BigDecimal SALARIO_MIN = new BigDecimal("0.00");
    private static final BigDecimal SALARIO_MAX = new BigDecimal("15000000.00");
    private static final Pattern EMAIL_RE =
            Pattern.compile("^[\\w.!#$%&’*+/=?`{|}~^-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserRepository userRepository;

    @Override
    public Mono<User> save(User u) {


        // --- Email obligatorio y normalización ---
        final String rawEmail = u.getEmail();
        if (rawEmail == null || rawEmail.isBlank()) {
            return Mono.error(new IllegalArgumentException("El correo_electronico es obligatorio"));
        }
        final String emailNorm = rawEmail.trim().toLowerCase(Locale.ROOT);
        u.setEmail(emailNorm);

        // --- Validación salario ---
        final BigDecimal s = u.getBaseSalary();
        if (s == null) {
            return Mono.error(new SalaryValidateException(
                    "El salario debe ser un valor numérico entre 0 y 15,000,000"));
        }
        if (s.scale() > 2) {
            return Mono.error(new SalaryValidateException("El salario admite máximo 2 decimales"));
        }
        if (s.compareTo(SALARIO_MIN) < 0 || s.compareTo(SALARIO_MAX) > 0) {
            return Mono.error(new SalaryValidateException(
                    "El salario debe estar entre 0 y 15,000,000"));
        }

        // --- Validación formato email ---
        if (!EMAIL_RE.matcher(emailNorm).matches()) {   // <-- matches(), no matche()
            return Mono.error(new IllegalArgumentException("Correo_electronico inválido"));
        }

        // --- Regla de negocio: unicidad email ---
        return userRepository.existsByMail(emailNorm)
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new EmailDuplicadoException(emailNorm));
                    }
                    return userRepository.save(u);
                })
                // Blindaje por condición de carrera: índice único en BD y mapeo a excepción de negocio
                .onErrorMap(t -> {
                    // Unwrap simple (sin traer clases de Spring): obtenemos la causa raíz
                    Throwable cause = t;
                    while (cause.getCause() != null) cause = cause.getCause();
                    if (cause instanceof UniqueConstraintViolationException ucv &&
                            "email".equalsIgnoreCase(ucv.getField())) {
                        return new EmailDuplicadoException(emailNorm);
                    }
                    return t;
                });
    }
}



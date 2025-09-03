package com.crediya.iam.usecase.user;

import com.crediya.iam.model.user.User;
import com.crediya.iam.model.user.gateways.UserRepository;
import com.crediya.iam.usecase.shared.ValidationException;
import com.crediya.iam.usecase.shared.security.PasswordService;
import com.crediya.iam.usecase.user.exceptions.EmailDuplicadoException;
import com.crediya.iam.usecase.user.gateway.TransactionGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {

    private TransactionGateway tx;
    private UserRepository userRepository;
    private PasswordService passwordService;
    private CreateUserUseCase useCase;

    @BeforeEach
    void setUp() {
        tx = mock(TransactionGateway.class);
        userRepository = mock(UserRepository.class);
        passwordService = mock(PasswordService.class);

        lenient().when(tx.required(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Supplier<Mono<User>> supplier =
                    invocation.getArgument(0, java.util.function.Supplier.class);
            return supplier.get();
        });

        useCase = new CreateUserUseCase(tx, userRepository, passwordService);
    }

    private User buildUser() {
        return User.create(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "123 Main St",
                "5551234",
                "john.doe@mail.com",
                java.math.BigDecimal.valueOf(5000),
                "123456789",          // ✅ documento válido (solo números)
                2L,
                "Passw0rd123"        // ✅ contraseña válida
        );
    }

    @Test
    void execute_shouldCreateUserSuccessfully() {
        User user = buildUser();

        when(userRepository.existsByMail("john.doe@mail.com")).thenReturn(Mono.just(false));
        when(passwordService.generatePasswordHash("Passw0rd123")).thenReturn(Mono.just("hashedPass"));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return Mono.just(u.withId(1L));
        });

        StepVerifier.create(useCase.execute(user))
                .expectNextMatches(saved ->
                        saved.getId().equals(1L)
                                && saved.getPassword().equals("hashedPass")
                                && saved.getEmail().equals("john.doe@mail.com"))
                .verifyComplete();
    }

    @Test
    void execute_shouldFailWhenEmailAlreadyExists() {
        User user = buildUser();

        when(userRepository.existsByMail("john.doe@mail.com")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.execute(user))
                .expectError(EmailDuplicadoException.class)
                .verify();

        verify(userRepository).existsByMail("john.doe@mail.com");
        verify(passwordService, never()).generatePasswordHash(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void execute_shouldFailWhenPasswordHashFails() {
        User user = buildUser();

        when(userRepository.existsByMail("john.doe@mail.com")).thenReturn(Mono.just(false));
        when(passwordService.generatePasswordHash("Passw0rd123"))
                .thenReturn(Mono.error(new RuntimeException("Hashing failed")));

        StepVerifier.create(useCase.execute(user))
                .expectErrorMatches(e -> e instanceof RuntimeException && e.getMessage().equals("Hashing failed"))
                .verify();

        verify(userRepository).existsByMail("john.doe@mail.com");
        verify(passwordService).generatePasswordHash("Passw0rd123");
        verify(userRepository, never()).save(any());
    }

    @Test
    void execute_shouldFailValidationIfUserIsInvalid() {
        User invalidUser = User.create(
                "", // nombre vacío -> falla
                "Doe",
                LocalDate.of(1990, 1, 1),
                "123 Main St",
                "5551234",
                "badmail", // ❌ email inválido
                java.math.BigDecimal.valueOf(1000),
                "ABC123",  // ❌ documento inválido
                2L,
                "pwd"      // ❌ contraseña inválida
        );

        StepVerifier.create(useCase.execute(invalidUser))
                .expectError(ValidationException.class) // ✅ ahora correcto
                .verify();

        verify(userRepository, never()).existsByMail(any());
        verify(passwordService, never()).generatePasswordHash(any());
        verify(userRepository, never()).save(any());
    }
}

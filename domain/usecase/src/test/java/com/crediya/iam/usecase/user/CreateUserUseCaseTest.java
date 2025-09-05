package com.crediya.iam.usecase.user;

import com.crediya.iam.model.user.User;
import com.crediya.iam.model.user.gateways.UserRepository;
import com.crediya.iam.usecase.shared.ValidationException;
import com.crediya.iam.usecase.shared.security.PasswordService;
import com.crediya.iam.usecase.user.exceptions.EmailDuplicadoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {

    private UserRepository userRepository;
    private PasswordService passwordService;
    private CreateUserUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordService = mock(PasswordService.class);

        // ✅ Inicializar el useCase correctamente
        useCase = new CreateUserUseCase(userRepository, passwordService);
    }

    private User buildUser() {
        return User.create(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "123 Main St",
                "5551234567",
                "john.doe@mail.com",
                BigDecimal.valueOf(5000.00),
                "123456789",
                2L,
                "Passw0rd123"
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

        // ✅ Verificar las interacciones
        verify(userRepository).existsByMail("john.doe@mail.com");
        verify(passwordService).generatePasswordHash("Passw0rd123");
        verify(userRepository).save(any(User.class));
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
                "",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "123 Main St",
                "5551234",
                "badmail",
                BigDecimal.valueOf(1000.00),
                "ABC123",
                2L,
                "pwd"
        );

        StepVerifier.create(useCase.execute(invalidUser))
                .expectError(ValidationException.class)
                .verify();

        verify(userRepository, never()).existsByMail(any());
        verify(passwordService, never()).generatePasswordHash(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void execute_shouldFailWhenRepositorySaveFails() {
        User user = buildUser();

        when(userRepository.existsByMail("john.doe@mail.com")).thenReturn(Mono.just(false));
        when(passwordService.generatePasswordHash("Passw0rd123")).thenReturn(Mono.just("hashedPass"));
        when(userRepository.save(any(User.class))).thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(useCase.execute(user))
                .expectErrorMatches(e -> e instanceof RuntimeException && e.getMessage().equals("Database error"))
                .verify();

        verify(userRepository).existsByMail("john.doe@mail.com");
        verify(passwordService).generatePasswordHash("Passw0rd123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void execute_shouldNormalizeEmailBeforeCheck() {
        User user = User.create(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "123 Main St",
                "5551234567",
                "JOHN.DOE@MAIL.COM",
                BigDecimal.valueOf(5000.00),
                "123456789",
                2L,
                "Passw0rd123"
        );

        when(userRepository.existsByMail("john.doe@mail.com")).thenReturn(Mono.just(false));
        when(passwordService.generatePasswordHash("Passw0rd123")).thenReturn(Mono.just("hashedPass"));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return Mono.just(u.withId(1L));
        });

        StepVerifier.create(useCase.execute(user))
                .expectNextMatches(saved ->
                        saved.getId().equals(1L)
                                && saved.getEmail().equals("john.doe@mail.com"))
                .verifyComplete();

        // ✅ Verificar que se buscó con email normalizado
        verify(userRepository).existsByMail("john.doe@mail.com");
    }

    @Test
    void execute_shouldHandleNullUser() {
        StepVerifier.create(useCase.execute(null))
                .expectError(ValidationException.class)
                .verify();

        verify(userRepository, never()).existsByMail(any());
        verify(passwordService, never()).generatePasswordHash(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void execute_shouldLogSuccessfulCreation() {
        User user = buildUser();

        when(userRepository.existsByMail("john.doe@mail.com")).thenReturn(Mono.just(false));
        when(passwordService.generatePasswordHash("Passw0rd123")).thenReturn(Mono.just("hashedPass"));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return Mono.just(u.withId(1L));
        });

        StepVerifier.create(useCase.execute(user))
                .expectNextCount(1)
                .verifyComplete();

        // ✅ Verificar que todas las operaciones se ejecutaron
        verify(userRepository).existsByMail("john.doe@mail.com");
        verify(passwordService).generatePasswordHash("Passw0rd123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void execute_shouldSetHashedPasswordOnUser() {
        User user = buildUser();
        String originalPassword = user.getPassword();

        when(userRepository.existsByMail("john.doe@mail.com")).thenReturn(Mono.just(false));
        when(passwordService.generatePasswordHash(originalPassword)).thenReturn(Mono.just("superSecureHash"));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return Mono.just(u.withId(1L));
        });

        StepVerifier.create(useCase.execute(user))
                .expectNextMatches(saved -> saved.getPassword().equals("superSecureHash"))
                .verifyComplete();

        verify(passwordService).generatePasswordHash(originalPassword);
    }
}
package com.crediya.iam.usecase.user;

import com.crediya.iam.model.user.User;
import com.crediya.iam.model.user.gateways.UserRepository;
import com.crediya.iam.usecase.user.exceptions.EmailDuplicadoException;
import com.crediya.iam.usecase.user.exceptions.SalaryValidateException;
import com.crediya.iam.usecase.user.gateway.TransactionGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class CreateUserUseCaseTest {

    @Mock
    UserRepository userRepository;

    TransactionGateway tx;

    CreateUserUseCase useCase;

    @BeforeEach
    void setUp() {
        // TransactionGateway de prueba: ejecuta el supplier tal cual (sin TX real)
        tx = new TransactionGateway() {
            @Override public <T> Mono<T> required(java.util.function.Supplier<Mono<T>> work) {
                return Mono.defer(work);
            }
            @Override public <T> reactor.core.publisher.Flux<T> requiredMany(java.util.function.Supplier<reactor.core.publisher.Flux<T>> work) {
                return reactor.core.publisher.Flux.defer(work);
            }
        };
        useCase = new CreateUserUseCase(tx, userRepository);
    }

    // ===== Helper =====
    private User buildUser(String email, BigDecimal salary, String doc, String phone) {
        Long roleId = 1L;
        return User.create(
                "Ana", "García",
                LocalDate.of(1990, 5, 10),
                "Calle 123",
                phone,
                email,
                salary,
                doc,
                roleId
        );
    }

    // ===== Tests =====

    @Test
    void errorWhenUserIsNull() {
        StepVerifier.create(useCase.execute(null))
                .expectError(IllegalArgumentException.class)
                .verify();
        verifyNoInteractions(userRepository);
    }

    @Test
    void errorWhenIdentityDocumentIsNull() {
        var u = buildUser("ana@example.com", BigDecimal.valueOf(1000), null, "3001234567");
        StepVerifier.create(useCase.execute(u))
                .expectError(IllegalArgumentException.class)
                .verify();
        verifyNoInteractions(userRepository);
    }

    @Test
    void errorWhenPhoneIsNullOrBlank() {
        var u1 = buildUser("ana@example.com", BigDecimal.valueOf(1000), "CC1", null);
        StepVerifier.create(useCase.execute(u1))
                .expectError(IllegalArgumentException.class)
                .verify();

        var u2 = buildUser("ana@example.com", BigDecimal.valueOf(1000), "CC1", "   ");
        StepVerifier.create(useCase.execute(u2))
                .expectError(IllegalArgumentException.class)
                .verify();

        verifyNoInteractions(userRepository);
    }

    @Test
    void errorWhenPhoneHasNonDigits() {
        var u = buildUser("ana@example.com", BigDecimal.valueOf(1000), "CC1", "300-123");
        StepVerifier.create(useCase.execute(u))
                .expectError(IllegalArgumentException.class)
                .verify();
        verifyNoInteractions(userRepository);
    }

    @Test
    void errorWhenEmailMissing() {
        var u = buildUser(null, BigDecimal.valueOf(1000), "CC1", "3001234567");
        StepVerifier.create(useCase.execute(u))
                .expectError(IllegalArgumentException.class)
                .verify();
        verifyNoInteractions(userRepository);
    }

    @Test
    void errorWhenEmailInvalidFormat() {
        var u = buildUser("bad-email", BigDecimal.valueOf(1000), "CC1", "3001234567");
        StepVerifier.create(useCase.execute(u))
                .expectError(IllegalArgumentException.class)
                .verify();
        verifyNoInteractions(userRepository);
    }

    @Nested
    class SalaryValidation {

        @Test
        void errorWhenSalaryIsNull() {
            var u = buildUser("ana@example.com", null, "CC1", "3001234567");
            StepVerifier.create(useCase.execute(u))
                    .expectError(SalaryValidateException.class)
                    .verify();
            verifyNoInteractions(userRepository);
        }

        @Test
        void errorWhenScaleGreaterThan2() {
            var u = buildUser("ana@example.com", new BigDecimal("123.456"), "CC1", "3001234567");
            StepVerifier.create(useCase.execute(u))
                    .expectError(SalaryValidateException.class)
                    .verify();
            verifyNoInteractions(userRepository);
        }

        @Test
        void errorWhenLessThanMin() {
            var u = buildUser("ana@example.com", new BigDecimal("-0.01"), "CC1", "3001234567");
            StepVerifier.create(useCase.execute(u))
                    .expectError(SalaryValidateException.class)
                    .verify();
            verifyNoInteractions(userRepository);
        }

        @Test
        void errorWhenGreaterThanMax() {
            var u = buildUser("ana@example.com", new BigDecimal("15000000.01"), "CC1", "3001234567");
            StepVerifier.create(useCase.execute(u))
                    .expectError(SalaryValidateException.class)
                    .verify();
            verifyNoInteractions(userRepository);
        }
    }

    @Test
    void errorWhenEmailDuplicate() {
        var u = buildUser("Ana@Example.com  ", BigDecimal.valueOf(1000), "CC1", "3001234567");

        when(userRepository.existsByMail("ana@example.com")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.execute(u))
                .expectError(EmailDuplicadoException.class)
                .verify();

        verify(userRepository, times(1)).existsByMail("ana@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void successSavesUserAndNormalizesEmail() {
        var u = buildUser("Ana@Example.com  ", BigDecimal.valueOf(1000), "CC1", "3001234567");

        when(userRepository.existsByMail("ana@example.com")).thenReturn(Mono.just(false));
        when(userRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.execute(u))
                .expectNextMatches(saved -> saved != null && "ana@example.com".equals(saved.getEmail()))
                .verifyComplete();

        verify(userRepository, times(1)).existsByMail("ana@example.com");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(captor.capture());
        User savedArg = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals("ana@example.com", savedArg.getEmail());
    }
}

package com.crediya.iam.usecase.user;


import com.crediya.iam.model.user.User;
import com.crediya.iam.model.user.gateways.UserRepository;
import com.crediya.iam.usecase.existuser.ExistUserUseCase;
import com.crediya.iam.usecase.shared.security.ExceptionGeneral;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExistUserUseCaseTest {

    private UserRepository userRepository;
    private ExistUserUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        useCase = new ExistUserUseCase(userRepository);
    }

    private User createTestUser(String email, String document) {
        return User.builder()
                .id(1L)
                .firstName("Test")
                .lastName("User")
                .email(email)
                .birthdate(LocalDate.of(1990, 1, 1))
                .identityDocument(document)
                .phoneNumber("1234567890")
                .baseSalary(BigDecimal.valueOf(5000))
                .address("Test Address")
                .roleId(1L)
                .active(true)
                .password("hashedPassword")
                .build();
    }

    @Test
    void execute_shouldReturnTrueWhenUserExistsAndEmailMatches() {
        String document = "123456789";
        String email = "test@example.com";
        User user = createTestUser(email, document);

        when(userRepository.existUserForDocument(document)).thenReturn(Mono.just(user));

        StepVerifier.create(useCase.execute(document, email))
                .expectNext(true)
                .verifyComplete();

        verify(userRepository).existUserForDocument(document);
    }

    @Test
    void execute_shouldReturnTrueWhenEmailMatchesIgnoreCase() {
        String document = "123456789";
        String storedEmail = "test@example.com";
        String inputEmail = "TEST@EXAMPLE.COM";
        User user = createTestUser(storedEmail, document);

        when(userRepository.existUserForDocument(document)).thenReturn(Mono.just(user));

        StepVerifier.create(useCase.execute(document, inputEmail))
                .expectNext(true)
                .verifyComplete();

        verify(userRepository).existUserForDocument(document);
    }

    @Test
    void execute_shouldFailWhenEmailsDontMatch() {
        String document = "123456789";
        String storedEmail = "test@example.com";
        String inputEmail = "different@example.com";
        User user = createTestUser(storedEmail, document);

        when(userRepository.existUserForDocument(document)).thenReturn(Mono.just(user));

        StepVerifier.create(useCase.execute(document, inputEmail))
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(userRepository).existUserForDocument(document);
    }

    @Test
    void execute_shouldFailWhenDocumentIsNull() {
        StepVerifier.create(useCase.execute(null, "test@example.com"))
                .expectError(ExceptionGeneral.class)
                .verify();

        verify(userRepository, never()).existUserForDocument(any());
    }

    @Test
    void execute_shouldFailWhenEmailIsNull() {
        StepVerifier.create(useCase.execute("123456789", null))
                .expectError(ExceptionGeneral.class)
                .verify();

        verify(userRepository, never()).existUserForDocument(any());
    }

    @Test
    void execute_shouldFailWhenUserNotFound() {
        String document = "123456789";
        String email = "test@example.com";

        when(userRepository.existUserForDocument(document)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(document, email))
                .expectError(ExceptionGeneral.class)
                .verify();

        verify(userRepository).existUserForDocument(document);
    }

    @Test
    void execute_shouldHandleUserWithNullEmail() {
        String document = "123456789";
        String inputEmail = "test@example.com";
        User user = createTestUser(null, document);

        when(userRepository.existUserForDocument(document)).thenReturn(Mono.just(user));

        StepVerifier.create(useCase.execute(document, inputEmail))
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(userRepository).existUserForDocument(document);
    }

    @Test
    void execute_shouldHandleRepositoryError() {
        String document = "123456789";
        String email = "test@example.com";
        RuntimeException repositoryError = new RuntimeException("Database error");

        when(userRepository.existUserForDocument(document)).thenReturn(Mono.error(repositoryError));

        StepVerifier.create(useCase.execute(document, email))
                .expectError(RuntimeException.class)
                .verify();

        verify(userRepository).existUserForDocument(document);
    }
}
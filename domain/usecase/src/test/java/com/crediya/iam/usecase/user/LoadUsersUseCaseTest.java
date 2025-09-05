package com.crediya.iam.usecase.user;


import com.crediya.iam.model.user.User;
import com.crediya.iam.model.user.gateways.UserRepository;
import com.crediya.iam.usecase.loadusers.LoadUsersUseCase;
import com.crediya.iam.usecase.shared.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoadUsersUseCaseTest {

    private UserRepository userRepository;
    private LoadUsersUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        useCase = new LoadUsersUseCase(userRepository);
    }

    private User createTestUser(Long id, String email) {
        return User.builder()
                .id(id)
                .firstName("Test")
                .lastName("User")
                .email(email)
                .birthdate(LocalDate.of(1990, 1, 1))
                .identityDocument("123456789")
                .phoneNumber("1234567890")
                .baseSalary(BigDecimal.valueOf(5000))
                .address("Test Address")
                .roleId(1L)
                .active(true)
                .password("hashedPassword")
                .build();
    }

    @Test
    void execute_shouldReturnAllUsers() {
        User user1 = createTestUser(1L, "user1@test.com");
        User user2 = createTestUser(2L, "user2@test.com");

        when(userRepository.findAll()).thenReturn(Flux.just(user1, user2));

        StepVerifier.create(useCase.execute())
                .expectNext(user1)
                .expectNext(user2)
                .verifyComplete();

        verify(userRepository).findAll();
    }

    @Test
    void execute_shouldFailWhenNoUsersFound() {
        when(userRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute())
                .expectError(ValidationException.class)
                .verify();

        verify(userRepository).findAll();
    }

    @Test
    void execute_shouldPropagateRepositoryError() {
        RuntimeException repositoryError = new RuntimeException("Database error");
        when(userRepository.findAll()).thenReturn(Flux.error(repositoryError));

        StepVerifier.create(useCase.execute())
                .expectError(RuntimeException.class)
                .verify();

        verify(userRepository).findAll();
    }
}
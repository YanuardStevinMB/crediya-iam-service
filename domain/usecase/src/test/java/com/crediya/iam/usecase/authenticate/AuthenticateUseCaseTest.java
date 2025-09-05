package com.crediya.iam.usecase.authenticate;

import com.crediya.iam.model.role.Role;
import com.crediya.iam.model.role.gateways.RoleRepository;
import com.crediya.iam.model.user.User;
import com.crediya.iam.model.user.gateways.UserRepository;
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
class AuthenticateUseCaseTest {

    private UserRepository userRepository;
    private PasswordHasherPort passwordHasher;
    private TokenGeneratorPort tokenGenerator;
    private RoleRepository roleRepository;
    private AuthenticateUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordHasher = mock(PasswordHasherPort.class);
        tokenGenerator = mock(TokenGeneratorPort.class);
        roleRepository = mock(RoleRepository.class);

        useCase = new AuthenticateUseCase(
                userRepository,
                passwordHasher,
                tokenGenerator,
                roleRepository
        );
    }

    private User createTestUser(String email) {
        return User.builder()
                .id(1L)
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

    private Role createTestRole(Long id, String name) {
        return Role.builder()
                .id(id)
                .name(name)
                .description("Test role")
                .build();
    }

    @Test
    void login_shouldAuthenticateSuccessfully() {
        String email = "test@example.com";
        String rawPassword = "password123";
        User user = createTestUser(email);
        Role role = createTestRole(1L, "USER");
        TokenResult expectedToken = new TokenResult("token123", "Bearer", 1234567890L);

        when(userRepository.findByEmail(email.toLowerCase())).thenReturn(Mono.just(user));
        when(passwordHasher.matches(rawPassword, "hashedPassword")).thenReturn(true);
        when(roleRepository.findById(1L)).thenReturn(Mono.just(role));
        when(tokenGenerator.generate(user, "USER")).thenReturn(Mono.just(expectedToken));

        StepVerifier.create(useCase.login(email, rawPassword))
                .expectNext(expectedToken)
                .verifyComplete();

        verify(userRepository).findByEmail(email.toLowerCase());
        verify(passwordHasher).matches(rawPassword, "hashedPassword");
        verify(roleRepository).findById(1L);
        verify(tokenGenerator).generate(user, "USER");
    }

    @Test
    void login_shouldNormalizeEmailToLowercase() {
        String email = "TEST@EXAMPLE.COM";
        String rawPassword = "password123";
        User user = createTestUser("test@example.com");
        Role role = createTestRole(1L, "USER");
        TokenResult expectedToken = new TokenResult("token123", "Bearer", 1234567890L);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Mono.just(user));
        when(passwordHasher.matches(rawPassword, "hashedPassword")).thenReturn(true);
        when(roleRepository.findById(1L)).thenReturn(Mono.just(role));
        when(tokenGenerator.generate(user, "USER")).thenReturn(Mono.just(expectedToken));

        StepVerifier.create(useCase.login(email, rawPassword))
                .expectNext(expectedToken)
                .verifyComplete();

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void login_shouldFailWhenEmailIsNull() {
        StepVerifier.create(useCase.login(null, "password123"))
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(userRepository, never()).findByEmail(any());
        verify(passwordHasher, never()).matches(any(), any());
    }

    @Test
    void login_shouldFailWhenEmailIsBlank() {
        StepVerifier.create(useCase.login("   ", "password123"))
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(userRepository, never()).findByEmail(any());
        verify(passwordHasher, never()).matches(any(), any());
    }

    @Test
    void login_shouldFailWhenPasswordIsNull() {
        StepVerifier.create(useCase.login("test@example.com", null))
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(userRepository, never()).findByEmail(any());
        verify(passwordHasher, never()).matches(any(), any());
    }

    @Test
    void login_shouldFailWhenPasswordIsBlank() {
        StepVerifier.create(useCase.login("test@example.com", "   "))
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(userRepository, never()).findByEmail(any());
        verify(passwordHasher, never()).matches(any(), any());
    }

    @Test
    void login_shouldFailWhenUserNotFound() {
        String email = "test@example.com";
        String rawPassword = "password123";

        when(userRepository.findByEmail(email)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.login(email, rawPassword))
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(userRepository).findByEmail(email);
        verify(passwordHasher, never()).matches(any(), any());
        verify(roleRepository, never()).findById(any());
        verify(tokenGenerator, never()).generate(any(), any());
    }

    @Test
    void login_shouldFailWhenPasswordDoesntMatch() {
        String email = "test@example.com";
        String rawPassword = "wrongpassword";
        User user = createTestUser(email);

        when(userRepository.findByEmail(email)).thenReturn(Mono.just(user));
        when(passwordHasher.matches(rawPassword, "hashedPassword")).thenReturn(false);

        StepVerifier.create(useCase.login(email, rawPassword))
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(userRepository).findByEmail(email);
        verify(passwordHasher).matches(rawPassword, "hashedPassword");
        verify(roleRepository, never()).findById(any());
        verify(tokenGenerator, never()).generate(any(), any());
    }

    @Test
    void login_shouldFailWhenRoleNotFound() {
        String email = "test@example.com";
        String rawPassword = "password123";
        User user = createTestUser(email);

        when(userRepository.findByEmail(email)).thenReturn(Mono.just(user));
        when(passwordHasher.matches(rawPassword, "hashedPassword")).thenReturn(true);
        when(roleRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.login(email, rawPassword))
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(userRepository).findByEmail(email);
        verify(passwordHasher).matches(rawPassword, "hashedPassword");
        verify(roleRepository).findById(1L);
        verify(tokenGenerator, never()).generate(any(), any());
    }

    @Test
    void login_shouldFailWhenTokenGenerationFails() {
        String email = "test@example.com";
        String rawPassword = "password123";
        User user = createTestUser(email);
        Role role = createTestRole(1L, "USER");

        when(userRepository.findByEmail(email)).thenReturn(Mono.just(user));
        when(passwordHasher.matches(rawPassword, "hashedPassword")).thenReturn(true);
        when(roleRepository.findById(1L)).thenReturn(Mono.just(role));
        when(tokenGenerator.generate(user, "USER")).thenReturn(Mono.error(new RuntimeException("Token generation failed")));

        StepVerifier.create(useCase.login(email, rawPassword))
                .expectError(RuntimeException.class)
                .verify();

        verify(userRepository).findByEmail(email);
        verify(passwordHasher).matches(rawPassword, "hashedPassword");
        verify(roleRepository).findById(1L);
        verify(tokenGenerator).generate(user, "USER");
    }
}
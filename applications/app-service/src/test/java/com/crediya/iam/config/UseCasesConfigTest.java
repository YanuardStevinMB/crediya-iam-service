package com.crediya.iam.config;

import com.crediya.iam.model.user.gateways.UserRepository;
import com.crediya.iam.usecase.user.CreateUserUseCase;
import com.crediya.iam.usecase.user.gateway.TransactionGateway;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class UseCasesConfigTest {

    @Test
    void testUseCaseBeansExist() {
        try (var ctx = new AnnotationConfigApplicationContext(TestConfig.class)) {
            CreateUserUseCase uc = ctx.getBean(CreateUserUseCase.class);
            assertNotNull(uc);
        }
    }

    @Configuration
    @Import(com.crediya.iam.config.UseCasesConfig.class)
    static class TestConfig {

        // Stub simple: ejecuta el trabajo "tal cual" (sin transacción real)
        @Bean
        TransactionGateway tx() {
            return new TransactionGateway() {
                @Override
                public <T> reactor.core.publisher.Mono<T> required(
                        java.util.function.Supplier<reactor.core.publisher.Mono<T>> work) {
                    return reactor.core.publisher.Mono.defer(work);
                }
                @Override
                public <T> reactor.core.publisher.Flux<T> requiredMany(
                        java.util.function.Supplier<reactor.core.publisher.Flux<T>> work) {
                    return reactor.core.publisher.Flux.defer(work);
                }
            };
        }

        // Mock del repositorio para satisfacer la dependencia
        @Bean
        UserRepository userRepository() {
            return org.mockito.Mockito.mock(UserRepository.class);
        }
    }
}

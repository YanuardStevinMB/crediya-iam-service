package com.crediya.iam.usecase.gateway;


import com.crediya.iam.usecase.user.gateway.TransactionGateway;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class TransactionGatewayTest {

    // Implementación dummy para ejecutar directamente los suppliers
    private final TransactionGateway gateway = new TransactionGateway() {
        @Override
        public <T> Mono<T> required(java.util.function.Supplier<Mono<T>> work) {
            return work.get();
        }

        @Override
        public <T> Flux<T> requiredMany(java.util.function.Supplier<Flux<T>> work) {
            return work.get();
        }
    };

    @Test
    void required_shouldExecuteSupplier() {
        Mono<String> result = gateway.required(() -> Mono.just("Hello"));

        StepVerifier.create(result)
                .expectNext("Hello")
                .verifyComplete();
    }

    @Test
    void requiredMany_shouldExecuteSupplier() {
        Flux<Integer> result = gateway.requiredMany(() -> Flux.just(1, 2, 3));

        StepVerifier.create(result)
                .expectNext(1, 2, 3)
                .verifyComplete();
    }

    @Test
    void requiredWith_shouldDelegateToRequired() {
        var opts = TransactionGateway.TransactionOptions.defaultOpts();

        Mono<String> result = gateway.requiredWith(opts, () -> Mono.just("Delegated"));

        StepVerifier.create(result)
                .expectNext("Delegated")
                .verifyComplete();
    }

    @Test
    void transactionOptions_shouldHoldValues() {
        var opts = new TransactionGateway.TransactionOptions(
                TransactionGateway.TransactionOptions.Isolation.SERIALIZABLE,
                true,
                Duration.ofSeconds(30)
        );

        assertEquals(TransactionGateway.TransactionOptions.Isolation.SERIALIZABLE, opts.isolation());
        assertTrue(opts.readOnly());
        assertEquals(Duration.ofSeconds(30), opts.timeout());
    }

    @Test
    void defaultOptions_shouldHaveExpectedDefaults() {
        var opts = TransactionGateway.TransactionOptions.defaultOpts();

        assertEquals(TransactionGateway.TransactionOptions.Isolation.DEFAULT, opts.isolation());
        assertFalse(opts.readOnly());
        assertNull(opts.timeout());
    }
}

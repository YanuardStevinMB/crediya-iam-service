package com.crediya.iam.usecase.user.gateway;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Supplier;

public interface TransactionGateway {
    <T> Mono<T> required(Supplier<Mono<T>> work);
    <T> Flux<T> requiredMany(Supplier<Flux<T>> work);

    // Opcional: control de aislamiento/timeout/readOnly
    default <T> Mono<T> requiredWith(TransactionOptions opts, Supplier<Mono<T>> work) {
        // Implementado en el adapter. Aquí opcionalmente puedes lanzar UnsupportedOperationException si no lo implementas.
        return required(work);
    }

    record TransactionOptions(Isolation isolation, boolean readOnly, Duration timeout) {
        public enum Isolation { DEFAULT, READ_COMMITTED, REPEATABLE_READ, SERIALIZABLE }
        public static TransactionOptions defaultOpts() {
            return new TransactionOptions(Isolation.DEFAULT, false, null);
        }
    }
}
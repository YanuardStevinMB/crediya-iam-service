// infrastructure/driven-adapters/src/main/java/com/crediya/iam/r2dbc/transaction/SpringTransactionGateway.java
package com.crediya.iam.r2dbc.transaction;

import com.crediya.iam.usecase.user.gateway.TransactionGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class SpringTransactionGateway implements TransactionGateway {

    private final ReactiveTransactionManager tm; // <-- inyectamos también el manager
    private final TransactionalOperator tx;      // bean por defecto (REQUIRED)

    @Override
    public <T> Mono<T> required(Supplier<Mono<T>> work) {
        return tx.transactional(Mono.defer(work));
    }

    @Override
    public <T> Flux<T> requiredMany(Supplier<Flux<T>> work) {
        return tx.transactional(Flux.defer(work));
    }

    @Override
    public <T> Mono<T> requiredWith(TransactionOptions opts, Supplier<Mono<T>> work) {
        var def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        // Aislamiento
        if (opts != null && opts.isolation() != null) {
            switch (opts.isolation()) {
                case READ_COMMITTED -> def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
                case REPEATABLE_READ -> def.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
                case SERIALIZABLE -> def.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
                default -> { /* DEFAULT */ }
            }
        }
        // readOnly
        if (opts != null) def.setReadOnly(opts.readOnly());
        // timeout
        if (opts != null && opts.timeout() != null) {
            def.setTimeout((int) opts.timeout().getSeconds());
        }

        var custom = TransactionalOperator.create(tm, def);
        return custom.transactional(Mono.defer(work));
    }
}

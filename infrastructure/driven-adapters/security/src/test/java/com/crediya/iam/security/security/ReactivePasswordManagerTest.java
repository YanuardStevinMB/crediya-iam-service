package com.crediya.iam.security.security;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.AssertionsKt.assertNotNull;

public class ReactivePasswordManagerTest {

    private final ReactivePasswordManager manager = new ReactivePasswordManager();

    @Test
    void generatePasswordHash_ok() {
        String raw = "S3guro#2025";

        StepVerifier.create(manager.generatePasswordHash(raw))
                .assertNext(hash -> {
                    assertNotNull(hash);
                    assertFalse(hash.isBlank());
                    // costo = 12 (como en tu clase), algunos BCrypt usan $2a$ o $2b$
                    assertTrue(
                            hash.startsWith("$2a$12$") || hash.startsWith("$2b$12$"),
                            "El hash no refleja el costo 12"
                    );
                })
                .verifyComplete();
    }

    @Test
    void generatePasswordHash_samePassword_producesDifferentHashes_dueToSalt() {
        String raw = "S3guro#2025";

        var m1 = manager.generatePasswordHash(raw);
        var m2 = manager.generatePasswordHash(raw);

        StepVerifier.create(m1.zipWith(m2))
                .assertNext(tuple -> {
                    String h1 = tuple.getT1();
                    String h2 = tuple.getT2();
                    assertNotEquals(h1, h2, "BCrypt debe generar hashes distintos por el salt");
                })
                .verifyComplete();
    }

    @Test
    void generatePasswordHash_null_throws() {
        StepVerifier.create(manager.generatePasswordHash(null))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().contains("no puede ser nula"))
                .verify();
    }

    @Test
    void generatePasswordHash_blank_throws() {
        StepVerifier.create(manager.generatePasswordHash("   "))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void matches_true_whenCorrectPassword() {
        String raw = "Clave#2025";
        var hashedMono = manager.generatePasswordHash(raw);

        StepVerifier.create(
                        hashedMono.flatMap(hash -> manager.matches(raw, hash))
                )
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void matches_false_whenWrongPassword() {
        String raw = "Clave#2025";
        var hashedMono = manager.generatePasswordHash(raw);

        StepVerifier.create(
                        hashedMono.flatMap(hash -> manager.matches("otraClave", hash))
                )
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void matches_invalidHash_returnsFalse() {
        String invalidHash = "$2a$12$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";

        StepVerifier.create(manager.matches("algo", invalidHash))
                .expectNext(false)
                .verifyComplete();
    }


}

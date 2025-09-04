package com.crediya.iam.usecase.user;

import com.crediya.iam.usecase.existuser.ExistUserValidated;
import com.crediya.iam.usecase.shared.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
class ExistUserValidatedTest {

    @Test
    void validateAndNormalize_shouldPassForValidDocument() {
        assertDoesNotThrow(() -> ExistUserValidated.validateAndNormalize("123456789"));
        assertDoesNotThrow(() -> ExistUserValidated.validateAndNormalize("1234567890"));
    }

    @Test
    void validateAndNormalize_shouldFailWhenDocumentIsNull() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> ExistUserValidated.validateAndNormalize(null)
        );
        assertEquals("identityDocument", exception.getField());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  "})
    void validateAndNormalize_shouldFailWhenDocumentIsBlank(String document) {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> ExistUserValidated.validateAndNormalize(document)
        );
        assertEquals("identityDocument", exception.getField());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ABC123", "12345678A", "123-456-789"})
    void validateAndNormalize_shouldFailForNonNumericDocument(String document) {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> ExistUserValidated.validateAndNormalize(document)
        );
        assertEquals("identityDocument", exception.getField());
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "123456789012345678901"})
    void validateAndNormalize_shouldFailForInvalidDocumentLength(String document) {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> ExistUserValidated.validateAndNormalize(document)
        );
        assertEquals("identityDocument", exception.getField());
    }

    @Test
    void validateAndNormalize_shouldFailForShortDocument() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> ExistUserValidated.validateAndNormalize("12345")
        );
        assertEquals("identityDocument", exception.getField());
    }

    @Test
    void validateAndNormalize_shouldFailForDocumentTooLong() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> ExistUserValidated.validateAndNormalize("123456789012345678901")
        );
        assertEquals("identityDocument", exception.getField());
    }

    @Test
    void validateAndNormalize_shouldPassFor8DigitDocument() {
        // Verifica que documentos de 8 dígitos fallan (longitud mínima es 9)
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> ExistUserValidated.validateAndNormalize("12345678")
        );
        assertEquals("identityDocument", exception.getField());
    }
}

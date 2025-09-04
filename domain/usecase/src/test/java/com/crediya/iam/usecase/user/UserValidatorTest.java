package com.crediya.iam.usecase.user;

import com.crediya.iam.model.user.User;
import com.crediya.iam.usecase.user.generaterequest.UserValidator;
import jakarta.xml.bind.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserValidatorTest {
    private User buildValidUser() {
        return User.create(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "123 Main St",
                "1234567890",
                "john.doe@example.com",
                BigDecimal.valueOf(5000.00),
                "123456789",
                1L,
                "Password123"
        );
    }

    @Test
    void validateAndNormalize_shouldPassForValidUser() {
        User user = buildValidUser();

        assertDoesNotThrow(() -> UserValidator.validateAndNormalize(user));
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals("123456789", user.getIdentityDocument());
        assertEquals("1234567890", user.getPhoneNumber());
    }






    @Test
    void validateAndNormalize_shouldNormalizeDocument() {
        User user = buildValidUser();
        user.setIdentityDocument("  1234567890  ");

        assertDoesNotThrow(() -> UserValidator.validateAndNormalize(user));
        assertEquals("1234567890", user.getIdentityDocument());
    }


    @Test
    void validateAndNormalize_shouldNormalizePhoneNumber() {
        User user = buildValidUser();
        user.setPhoneNumber("  3001234567  ");

        assertDoesNotThrow(() -> UserValidator.validateAndNormalize(user));
        assertEquals("3001234567", user.getPhoneNumber());
    }



    @Test
    void validateAndNormalize_shouldNormalizeEmail() {
        User user = buildValidUser();
        user.setEmail("  JOHN.DOE@EXAMPLE.COM  ");

        assertDoesNotThrow(() -> UserValidator.validateAndNormalize(user));
        assertEquals("john.doe@example.com", user.getEmail());
    }






}

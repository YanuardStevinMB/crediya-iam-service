package com.crediya.iam.usecase.authenticate;

public interface PasswordHasherPort {
    boolean matches(String raw, String hashed);
}
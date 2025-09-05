package com.crediya.iam.security.jwt;


import com.crediya.iam.usecase.authenticate.PasswordHasherPort;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordHasherAdapter implements PasswordHasherPort {

    private final PasswordEncoder encoder;

    public PasswordHasherAdapter(PasswordEncoder encoder) {
        this.encoder = encoder;
    }


    @Override
    public boolean matches(String raw, String hashed) {
        return encoder.matches(raw, hashed);
    }
}
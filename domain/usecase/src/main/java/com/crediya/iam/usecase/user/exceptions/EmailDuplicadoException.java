package com.crediya.iam.usecase.user.exceptions;

public class EmailDuplicadoException extends RuntimeException {
    private final String email;
    public EmailDuplicadoException(String email) {
        super("El correo_electronico ya está registrado");
        this.email = email;
    }
    public String getEmail() { return email; }
    public String getCode()  { return "EMAIL_DUPLICADO"; }
}
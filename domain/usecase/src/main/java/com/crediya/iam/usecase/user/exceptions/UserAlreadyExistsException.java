package com.crediya.iam.usecase.user.exceptions;

public class UserAlreadyExistsException  extends ServiceException {
    private final String email;

    public UserAlreadyExistsException(String email) {
        super("USER_ALREADY_EXISTS", "Ya existe un usuario con el correo " + email);
        this.email = email;
    }

    public String getEmail() { return email; }
}

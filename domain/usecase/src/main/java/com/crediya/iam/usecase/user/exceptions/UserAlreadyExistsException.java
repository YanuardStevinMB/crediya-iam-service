package com.crediya.iam.usecase.user.exceptions;

import com.crediya.iam.usecase.shared.Messages;

public class UserAlreadyExistsException  extends ServiceException {
    private final String email;

    public UserAlreadyExistsException(String email) {
        super("USER_ALREADY_EXISTS", Messages.USER_EXIST + email);
        this.email = email;
    }

    public String getEmail() { return email; }
}

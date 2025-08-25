package com.crediya.iam.usecase.user.exceptions;


public class UniqueConstraintViolationException extends RuntimeException {
    private final String field; // ej. "email"
    private final String value; // ej. "ana@example.com"

    public UniqueConstraintViolationException(String field, String value, String message) {
        super(message);
        this.field = field;
        this.value = value;
    }

    public String getField() { return field; }
    public String getValue() { return value; }
}

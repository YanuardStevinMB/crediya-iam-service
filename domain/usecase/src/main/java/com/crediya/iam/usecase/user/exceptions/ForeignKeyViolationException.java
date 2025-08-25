package com.crediya.iam.usecase.user.exceptions;

public class ForeignKeyViolationException extends RuntimeException {
    private final String field; // p.ej. "role_id"
    private final Object value; // p.ej. 7
    public ForeignKeyViolationException(String field, Object value, String message) {
        super(message);
        this.field = field;
        this.value = value;
    }
    public String getField() { return field; }
    public Object getValue() { return value; }
}
package com.crediya.iam.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private Object errors;    // puede ser String, Map<String,String>, List<FieldError>, etc.
    private String path;
    private Instant timestamp;

    @Data
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
    }

    public static <T> ApiResponse<T> ok(T data, String message, String path) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .errors(null)
                .path(path)
                .timestamp(Instant.now())
                .build();
    }

    public static ApiResponse<?> fail(String message, Object errors, String path) {
        return ApiResponse.builder()
                .success(false)
                .message(message)
                .data(null)
                .errors(errors)
                .path(path)
                .timestamp(Instant.now())
                .build();
    }

    // Opción B: si quieres conservar 'badRequest(...)' como alias:
    public static ApiResponse<?> badRequest(Object errors, String message, String path) {
        return fail(message, errors, path);
    }
}

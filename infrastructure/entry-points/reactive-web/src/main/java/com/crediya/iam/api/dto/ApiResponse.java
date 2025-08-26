package com.crediya.iam.api.dto;


import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private Object errors;    // puede ser String, Map<String, String>, List<...>, etc.
    private String path;
    private Instant timestamp;

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
}

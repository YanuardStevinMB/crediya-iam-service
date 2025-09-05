package com.crediya.iam.api;


import com.crediya.iam.api.dto.ApiResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Map;

public final class ApiErrorUtils {

    private ApiErrorUtils() { }

    public static Mono<ServerResponse> respond(ServerRequest req,
                                               HttpStatus status,
                                               String message,
                                               Object errors) {
        var body = ApiResponse.fail(message, errors, req.path());
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body);
    }




}

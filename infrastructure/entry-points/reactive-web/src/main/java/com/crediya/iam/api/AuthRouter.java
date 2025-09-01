package com.crediya.iam.api;

import com.crediya.iam.api.controller.AuthHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

public class AuthRouter {

    @Bean

    public RouterFunction<ServerResponse> authRoutes(AuthHandler handler,
                                                     ApiErrorFilter errorFilter) {
        return route(POST("/api/v1/login"), handler::login)
                .filter(errorFilter);
    }

}

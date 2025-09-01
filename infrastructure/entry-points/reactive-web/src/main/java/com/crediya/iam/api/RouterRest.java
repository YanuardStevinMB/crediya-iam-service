package com.crediya.iam.api;

import com.crediya.iam.api.controller.AuthHandler;
import com.crediya.iam.api.controller.UserHandler;
import com.crediya.iam.api.controller.UserValidatedExistHandler;
import com.crediya.iam.api.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@Tag(name = "IAM API", description = "Endpoints de autenticación y usuarios")
public class RouterRest {

    @Bean
    @RouterOperations({
            // GET /api/usecase/path
            @RouterOperation(
                    path = "/api/usecase/path",
                    method = RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "listenGETUseCase",
                    operation = @Operation(
                            operationId = "getUseCase",
                            summary = "Consultar caso de uso (GET)",
                            description = "Retorna datos del caso de uso (ejemplo GET)",
                            tags = {"IAM API"}
                    )
            ),
            // POST /api/cxc  -> Crear usuario
            @RouterOperation(
                    path = "/api/v1/usuarios",
                    method = RequestMethod.POST,
                    beanClass = UserHandler.class,
                    beanMethod = "save",
                    operation = @Operation(
                            operationId = "createUser",
                            summary = "Crear usuario",
                            description = "Crea un usuario en el sistema",
                            tags = {"IAM API"},
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            schema = @Schema(implementation = UserSaveDto.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Usuario creado",
                                            content = @Content(
                                                    schema = @Schema(implementation = UserResponseDto.class)
                                            )
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Error de validación"),
                                    @ApiResponse(responseCode = "409", description = "Email duplicado")
                            }
                    )
            ),


            // POST /api/v1/login
            @RouterOperation(
                    path = "/api/v1/login",
                    method = RequestMethod.POST,
                    beanClass = AuthHandler.class,
                    beanMethod = "login",


                    operation = @Operation(
                            operationId = "login",
                            summary = "Login de usuario",
                            description = "Autentica al usuario y retorna un token JWT",
                            tags = {"IAM API"},
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            schema = @Schema(implementation = LoginRequestDto.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Usuario creado",
                                            content = @Content(
                                                    schema = @Schema(implementation = LoginResponseDto.class)
                                            )
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Autenticación exitosa"),
                                    @ApiResponse(responseCode = "409", description = "Credenciales inválidas")
                            }
                    )

            ),
            // POST /api/usecase/path
            @RouterOperation(
                    path = "/api/v1/users/exist",
                    method = RequestMethod.POST,
                    beanClass = UserValidatedExistHandler.class,
                    beanMethod = "loadExistUser",
                    operation = @Operation(
                            operationId = "existUser",
                            summary = "Validación de usuario",
                            description = "Valida si un usuario existe a través de documento y correo",
                            tags = {"IAM API"},
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            schema = @Schema(implementation = UserExistRequestDto.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Cliente validado",
                                            content = @Content(
                                                    schema = @Schema(implementation = ApiResponse.class)
                                            )
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Error de validación"),
                                    @ApiResponse(responseCode = "409", description = "Correo no pertenece al documento registrado")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(
            Handler handler,
            UserHandler userHandler,
            AuthHandler authHandler,
            UserValidatedExistHandler userValidatedExistHandler
    ) {
        return route(
                GET("/api/usecase/path"), handler::listenGETUseCase)
                .andRoute(POST("/api/v1/usuarios"), userHandler::save)
                .andRoute(POST("/api/v1/users/exist"), userValidatedExistHandler::loadExistUser)
                .andRoute(POST("/api/v1/login"), authHandler::login);
    }
}

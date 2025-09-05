package com.crediya.iam.api.config.controller;

import com.crediya.iam.api.controller.UserValidatedExistHandler;
import com.crediya.iam.usecase.existuser.ExistUserUseCase;
import com.crediya.iam.usecase.shared.Messages;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.*;

import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@ExtendWith(MockitoExtension.class)
class UserValidatedExistHandlerTest {

    @Mock
    private ExistUserUseCase existUserUseCase;

    private UserValidatedExistHandler handler;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        handler = new UserValidatedExistHandler(existUserUseCase);
        RouterFunction<ServerResponse> router =
                route(POST("/api/v1/users/exist"), handler::loadExistUser);
        client = WebTestClient.bindToRouterFunction(router).build();
    }

    @Test
    void loadExistUser_shouldReturnOkTrue_whenUserExists() {
        when(existUserUseCase.execute("123", "a@b.com")).thenReturn(Mono.just(true));

        client.post()
                .uri("/api/v1/users/exist")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"document\":\"123\",\"email\":\"a@b.com\"}")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data").isEqualTo(true)
                .jsonPath("$.message").isEqualTo(Messages.USER_ALREADY_EXIST)
                .jsonPath("$.path").isEqualTo("/api/v1/users/exist");

        // verifica parámetros con los que se invocó el use case
        ArgumentCaptor<String> docCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> emailCap = ArgumentCaptor.forClass(String.class);
        verify(existUserUseCase).execute(docCap.capture(), emailCap.capture());
        assertEquals("123", docCap.getValue());
        assertEquals("a@b.com", emailCap.getValue());
    }

    @Test
    void loadExistUser_shouldReturnOkFalse_whenUserNotExists() {
        when(existUserUseCase.execute("456", "x@y.com")).thenReturn(Mono.just(false));

        client.post()
                .uri("/api/v1/users/exist")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"document\":\"456\",\"email\":\"x@y.com\"}")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data").isEqualTo(false)
                .jsonPath("$.message").isEqualTo(Messages.USER_NOT_EXIST)
                .jsonPath("$.path").isEqualTo("/api/v1/users/exist");
    }

    @Test
    void loadExistUser_shouldReturnOkFalse_whenBodyIsEmpty() {
        // switchIfEmpty -> responde OK con exists=false y mensaje USER_NOT_EXIST
        client.post()
                .uri("/api/v1/users/exist")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data").isEqualTo(false)
                .jsonPath("$.message").isEqualTo(Messages.USER_NOT_EXIST)
                .jsonPath("$.path").isEqualTo("/api/v1/users/exist");

        // no debe llamar al use case si no hay body
        verify(existUserUseCase, never()).execute(anyString(), anyString());
    }

    @Test
    void loadExistUser_shouldLogMethodPathAndDocument() {
        // Capturamos logs del handler (@Slf4j)
        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(UserValidatedExistHandler.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        when(existUserUseCase.execute("123", "a@b.com")).thenReturn(Mono.just(true));

        client.post()
                .uri("/api/v1/users/exist")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"document\":\"123\",\"email\":\"a@b.com\"}")
                .exchange()
                .expectStatus().isOk();

        boolean found = appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(msg -> msg.contains("[POST] /api/v1/users/exist -> Checking user existence for document=123"));
        assertTrue(found, "Debería loguear método, path y document");

        logger.detachAppender(appender);
    }
}

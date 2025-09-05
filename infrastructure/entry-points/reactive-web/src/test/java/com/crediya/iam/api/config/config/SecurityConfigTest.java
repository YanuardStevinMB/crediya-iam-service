package com.crediya.iam.api.config.config;

import com.crediya.iam.api.config.BearerSecurityContextRepository;
import com.crediya.iam.api.config.SecurityConfig;
import com.crediya.iam.security.jwt.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private BearerSecurityContextRepository contextRepo;

    @Mock
    private JwtProperties jwtProperties;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(contextRepo);
    }

    @Test
    void springSecurityFilterChain_shouldCreateValidFilterChain() {
        ServerHttpSecurity http = ServerHttpSecurity.http();

        SecurityWebFilterChain filterChain = securityConfig.springSecurityFilterChain(http);

        assertNotNull(filterChain);
        // Verificar que el filter chain no sea nulo y tenga configuraciones
        assertTrue(filterChain.getWebFilters().collectList().block().size() > 0);
    }

    @Test
    void contextRepository_shouldBeInjected() {
        assertNotNull(securityConfig);
        // Verificar que la dependencia fue inyectada correctamente
        Object injectedContextRepo = ReflectionTestUtils.getField(securityConfig, "contextRepo");
        assertEquals(contextRepo, injectedContextRepo);
    }


    @Test
    void springSecurityFilterChain_shouldHaveBeanAnnotation() throws NoSuchMethodException {
        // Verificar que el método tiene la anotación @Bean
        assertTrue(SecurityConfig.class.getMethod("springSecurityFilterChain", ServerHttpSecurity.class)
                .isAnnotationPresent(org.springframework.context.annotation.Bean.class));
    }
}

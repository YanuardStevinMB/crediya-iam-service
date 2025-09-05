package com.crediya.iam.api.config;

import com.crediya.iam.security.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(JwtProperties .class) // expone tus props de JWT
@RequiredArgsConstructor
public class SecurityConfig {
    private final BearerSecurityContextRepository contextRepo;


    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)   // sin Basic
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)   // sin formulario
                .securityContextRepository(contextRepo)                  // tu Bearer repo
                .authorizeExchange(ex -> ex
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Swagger completo abierto
                        .pathMatchers("/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/webjars/**",
                                "/actuator/**").permitAll()
                        // regla de lista
                        .pathMatchers(HttpMethod.GET, "/api/v1/usuarios").permitAll()
                        // Login abierto
                        .pathMatchers(HttpMethod.POST, "/api/v1/login").permitAll()
                        // Regla por rol
                        .pathMatchers(HttpMethod.POST, "/api/v1/usuarios").hasAnyRole("ADMIN","ASESOR")
                        // Regla datos de usuario
                        .pathMatchers(HttpMethod.POST, "/api/v1/users/exist").hasAnyRole("CLIENTE")
                        // Resto autenticado
                        .anyExchange().authenticated()
                )
                // ⬇️ esto evita que Spring intente “/login”
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((exchange, ex) ->
                                Mono.fromRunnable(() -> exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED)))
                        .accessDeniedHandler((exchange, ex) ->
                                Mono.fromRunnable(() -> exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN)))
                )
                .build();
    }

}

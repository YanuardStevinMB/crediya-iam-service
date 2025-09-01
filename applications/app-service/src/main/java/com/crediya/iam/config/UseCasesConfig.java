package com.crediya.iam.config;

import com.crediya.iam.usecase.authenticate.TokenGeneratorPort;
import com.crediya.iam.usecase.shared.security.PasswordService;
import jwt.*;
import com.crediya.iam.usecase.authenticate.PasswordHasherPort;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import security.ReactivePasswordManager;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)

@ComponentScan(basePackages = "com.crediya.iam.usecase",
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "^.+UseCase$")
        },
        useDefaultFilters = false)

public class UseCasesConfig {

    // ===== JWT (ya lo tenías) =====
    @Bean
    public JwtReactiveAuthenticationManager jwtReactiveAuthenticationManager(JwtProperties props) {
        return new JwtReactiveAuthenticationManager(props);
    }



    // ===== Password hashing (LO QUE FALTABA) =====
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Ajusta la fuerza según tu entorno (>= 10 en prod)
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public PasswordHasherPort passwordHasherPort(PasswordEncoder encoder) {
        // Expones el puerto con su implementación de infraestructura
        return new PasswordHasherAdapter(encoder);
    }


    @Bean
    public PasswordService passwordService() {
        return new ReactivePasswordManager();
    }
    @Bean
    public TokenGeneratorPort tokenGeneratorPort(JwtProperties props) {
        return new JwtTokenGeneratorAdapter(props);
    }
}

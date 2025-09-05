package com.crediya.iam.config;

import com.crediya.iam.security.jwt.JwtProperties;

import com.crediya.iam.security.jwt.JwtReactiveAuthenticationManager;
import com.crediya.iam.usecase.authenticate.PasswordHasherPort;
import com.crediya.iam.security.jwt.PasswordHasherAdapter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
@ComponentScan(
        basePackages = "com.crediya.iam.usecase",
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "^.+UseCase$")
        },
        useDefaultFilters = false
)
public class UseCasesConfig {

    /**
     * Bean para codificar contraseñas usando BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Implementación del puerto de hashing de contraseñas usando el adaptador del módulo security
     */
    @Bean
    public PasswordHasherPort passwordHasherPort(PasswordEncoder passwordEncoder) {
        return new PasswordHasherAdapter(passwordEncoder);
    }

    /**
     * Bean para manejar autenticación JWT reactiva
     */
    @Bean
    public JwtReactiveAuthenticationManager jwtReactiveAuthenticationManager(JwtProperties props) {
        return new JwtReactiveAuthenticationManager(props);
    }

}

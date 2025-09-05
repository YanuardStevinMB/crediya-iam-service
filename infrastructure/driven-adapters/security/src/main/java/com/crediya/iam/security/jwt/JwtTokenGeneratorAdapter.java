package com.crediya.iam.security.jwt;

import com.crediya.iam.model.user.User;

import com.crediya.iam.usecase.authenticate.TokenGeneratorPort;
import com.crediya.iam.usecase.authenticate.TokenResult;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtTokenGeneratorAdapter implements TokenGeneratorPort {

    private final JwtProperties props;
    private final SecretKey key;

    public JwtTokenGeneratorAdapter(JwtProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<TokenResult> generate(User user, String role) {
        long now = Instant.now().getEpochSecond();
        long exp = now + props.getExpirationSec();

        log.info("Generando token para usuario: {} con rol: {}", user.getEmail(), role);
        List<String> roles = List.of(role); // <- SIN "ROLE_"

        String token = Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setIssuer(props.getIssuer())
                .setIssuedAt(new Date(now * 1000))
                .setExpiration(new Date(exp * 1000))
                .claim("email", user.getEmail())
                .claim("roles", roles)           // <-- rol aplicado
                .claim("roleId", user.getRoleId())
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        log.info("Token generado para usuario {}: {}", user.getEmail(), token);

        return Mono.just(new TokenResult(token, "Bearer", exp));
    }
}

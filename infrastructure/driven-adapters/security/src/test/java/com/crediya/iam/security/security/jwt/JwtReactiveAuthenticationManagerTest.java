package com.crediya.iam.security.security.jwt;

import com.crediya.iam.security.jwt.JwtProperties;
import com.crediya.iam.security.jwt.JwtReactiveAuthenticationManager;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

class JwtReactiveAuthenticationManagerTest {

    private JwtReactiveAuthenticationManager manager;
    private JwtProperties props;

    @BeforeEach
    void setUp() {
        props = new JwtProperties();
        props.setSecret("mysupersecretmysupersecretmysupersecret"); // >= 32 chars
        props.setIssuer("crediya");
        manager = new JwtReactiveAuthenticationManager(props);
    }

    private String generateToken(Map<String, Object> claims, String subject, String issuer) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuer(issuer)
                .setIssuedAt(new Date())
                .signWith(
                        io.jsonwebtoken.security.Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8)),
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    @Test
    void authenticate_shouldReturnAuthenticationWithRoles() {
        String token = generateToken(Map.of("roles", List.of("ADMIN", "USER")), "user123", "crediya");

        Authentication auth = new UsernamePasswordAuthenticationToken("ignored", token);

        StepVerifier.create(manager.authenticate(auth))
                .expectNextMatches(a ->
                        a.getName().equals("user123") &&
                                a.getAuthorities().size() == 2
                )
                .verifyComplete();
    }

    @Test
    void authenticate_shouldMapRoleIdToAuthorities() {
        String token = generateToken(Map.of("roleId", "2"), "user456", "crediya");

        Authentication auth = new UsernamePasswordAuthenticationToken("ignored", token);

        StepVerifier.create(manager.authenticate(auth))
                .expectNextMatches(a ->
                        a.getName().equals("user456") &&
                                a.getAuthorities().stream().anyMatch(ga -> ga.getAuthority().equals("ROLE_ASESOR"))
                )
                .verifyComplete();
    }

    @Test
    void authenticate_shouldReturnEmptyWhenIssuerDoesNotMatch() {
        String token = generateToken(Map.of("roles", List.of("ADMIN")), "user789", "otro-issuer");

        Authentication auth = new UsernamePasswordAuthenticationToken("ignored", token);

        StepVerifier.create(manager.authenticate(auth))
                .verifyComplete(); // No emite Authentication
    }

    @Test
    void authenticate_shouldReturnEmptyWhenTokenIsNull() {
        Authentication auth = new UsernamePasswordAuthenticationToken("ignored", null);

        StepVerifier.create(manager.authenticate(auth))
                .verifyComplete();
    }

    @Test
    void authenticate_shouldReturnEmptyWhenTokenIsBlank() {
        Authentication auth = new UsernamePasswordAuthenticationToken("ignored", "   ");

        StepVerifier.create(manager.authenticate(auth))
                .verifyComplete();
    }

    @Test
    void authenticate_shouldReturnAuthenticationWithoutAuthoritiesIfNoRolesOrRoleId() {
        String token = generateToken(Map.of(), "user000", "crediya");

        Authentication auth = new UsernamePasswordAuthenticationToken("ignored", token);

        StepVerifier.create(manager.authenticate(auth))
                .expectNextMatches(a ->
                        a.getName().equals("user000") && a.getAuthorities().isEmpty()
                )
                .verifyComplete();
    }
}
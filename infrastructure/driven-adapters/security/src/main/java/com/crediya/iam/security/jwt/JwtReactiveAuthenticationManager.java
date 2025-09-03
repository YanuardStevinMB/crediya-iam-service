package com.crediya.iam.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final SecretKey key;
    private final JwtProperties props;

    public JwtReactiveAuthenticationManager(JwtProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = (String) authentication.getCredentials();
        if (token == null || token.isBlank()) return Mono.empty();

        try {
            var parser = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .setAllowedClockSkewSeconds(Duration.ofSeconds(30).toSeconds())
                    .build();

            Jws<Claims> jws = parser.parseClaimsJws(token);
            Claims claims = jws.getBody();

            if (props.getIssuer() != null && !props.getIssuer().isBlank()) {
                String iss = claims.getIssuer();
                if (iss == null || !iss.equals(props.getIssuer())) return Mono.empty();
            }

            String userId = claims.getSubject();
            if (userId == null || userId.isBlank()) return Mono.empty();

            // 1) primero intenta roles como lista de strings
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) claims.get("roles");
            if (roles == null || roles.isEmpty()) {
                // 2) si no hay, intenta con roleId y mapea
                Object rid = claims.get("roleId");
                String role = mapRoleIdToName(rid);
                roles = role == null ? List.of() : List.of(role);
            }

            var authorities = (roles == null ? Collections.<String>emptyList() : roles).stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                    .collect(Collectors.toList());

            var auth = new UsernamePasswordAuthenticationToken(userId, token, authorities);
            return Mono.just(auth);

        } catch (Exception e) {
            return Mono.empty();
        }
    }

    private String mapRoleIdToName(Object rid) {
        if (rid == null) return null;
        String s = String.valueOf(rid);
        return switch (s) {
            case "3" -> "ADMIN";
            case "2" -> "ASESOR";
            case "1" -> "CLIENTE";
            default -> null;
        };
    }
}

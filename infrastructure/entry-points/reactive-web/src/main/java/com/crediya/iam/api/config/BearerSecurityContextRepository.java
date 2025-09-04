package com.crediya.iam.api.config;

import com.crediya.iam.security.jwt.JwtReactiveAuthenticationManager;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class BearerSecurityContextRepository implements ServerSecurityContextRepository {

    private final JwtReactiveAuthenticationManager authManager;

    public BearerSecurityContextRepository(JwtReactiveAuthenticationManager authManager) {
        this.authManager = authManager;
    }

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        // Stateless, no guardamos nada
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        var req = exchange.getRequest();
        String auth = req.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        String token = null;
        if (auth != null && auth.startsWith("Bearer ")) {
            token = auth.substring(7);
        } else {
            // (opcional) soportar ?access_token=...
            token = req.getQueryParams().getFirst("access_token");
        }

        if (token == null || token.isBlank()) {
            return Mono.empty();
        }

        // El manager usa credentials (el token); el principal se ignora aquí
        var pre = new UsernamePasswordAuthenticationToken("N/A", token);
        return authManager.authenticate(pre).map(SecurityContextImpl::new);
    }
}

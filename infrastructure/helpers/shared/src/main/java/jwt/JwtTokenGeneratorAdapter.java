package jwt;

import com.crediya.iam.model.user.User;
import com.crediya.iam.usecase.authenticate.TokenGeneratorPort;
import com.crediya.iam.usecase.authenticate.TokenResult;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenGeneratorAdapter implements TokenGeneratorPort {

    private final JwtProperties props;
    private final SecretKey key;

    public JwtTokenGeneratorAdapter(JwtProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<TokenResult> generate(User user) {
        long now = Instant.now().getEpochSecond();
        long exp = now + props.getExpirationSec();

        // Si tu User NO tiene roles por nombre, ajusta este mapeo:
        // Ejemplo: roleId 1=CLIENTE, 2=ASESOR, 3=ADMIN (ajusta a tu realidad)
        List<String> roles = switch (String.valueOf(user.getRoleId())) {
            case "3" -> List.of("ADMIN");
            case "1" -> List.of("ADMIN");
            case "2" -> List.of("ASESOR");
            default -> List.of("CLIENTE");
        };

        String token = Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setIssuer(props.getIssuer())
                .setIssuedAt(new Date(now * 1000))
                .setExpiration(new Date(exp * 1000))
                .claim("email", user.getEmail())
                .claim("roles", roles)           // <-- lista de strings
                .claim("roleId", user.getRoleId()) // <-- por si quieres usar ID
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return Mono.just(new TokenResult(token, "Bearer", exp));
    }
}

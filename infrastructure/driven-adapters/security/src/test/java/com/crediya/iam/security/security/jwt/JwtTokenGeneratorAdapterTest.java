package com.crediya.iam.security.security.jwt;

import com.crediya.iam.model.user.User;
import com.crediya.iam.security.jwt.JwtProperties;
import com.crediya.iam.security.jwt.JwtTokenGeneratorAdapter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class JwtTokenGeneratorAdapterTest {

    private JwtProperties props;
    private JwtTokenGeneratorAdapter adapter;

    // Secret >= 32 bytes para HS256
    private static final String SECRET = "super-secreto-de-pruebas-32-bytes-minimos!!";
    private static final String ISSUER = "iam-test";
    private static final long   EXP_SEC = 3600L; // 1 hora

    @BeforeEach
    void setUp() {
        props = Mockito.mock(JwtProperties.class);
        when(props.getSecret()).thenReturn(SECRET);
        when(props.getIssuer()).thenReturn(ISSUER);
        when(props.getExpirationSec()).thenReturn(EXP_SEC);

        adapter = new JwtTokenGeneratorAdapter(props);
    }

    private User dummyUser() {
        // Ajusta según tu modelo real (getId, getEmail, getRoleId…)
        User u = new User();
        u.setId(123L);
        u.setEmail("ana@example.com");
        u.setRoleId(2L);
        return u;
    }

    @Test
    void generate_buildsValidJwt_withExpectedClaims() {
        var user = dummyUser();
        String role = "ADMIN";
        long before = Instant.now().getEpochSecond();

        StepVerifier.create(adapter.generate(user, role))
                .assertNext(res -> {
                    // TokenResult básico
                    assertThat(res).isNotNull();
                    assertThat(res.tokenType()).isEqualTo("Bearer");


                    // Parseamos el JWT para validar claims
                    SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
                    Jws<Claims> jws = Jwts.parserBuilder()
                            .setSigningKey(key)
                            .build()
                            .parseClaimsJws(res.token());

                    Claims c = jws.getBody();

                    // Subject (userId)
                    assertThat(c.getSubject()).isEqualTo(String.valueOf(user.getId()));
                    // Issuer
                    assertThat(c.getIssuer()).isEqualTo(ISSUER);
                    // Email
                    assertThat(c.get("email", String.class)).isEqualTo("ana@example.com");
                    // roleId
                    assertThat(String.valueOf(c.get("roleId"))).isEqualTo("2");

                    // roles como lista de strings
                    Object rolesClaim = c.get("roles");
                    assertThat(rolesClaim).isInstanceOf(List.class);
                    @SuppressWarnings("unchecked")
                    List<String> roles = (List<String>) rolesClaim;
                    assertThat(roles).containsExactly("ADMIN"); // sin prefijo

                    // Expiración coherente (tolerancia +/- 5s)
                    long expSec = c.getExpiration().toInstant().getEpochSecond();
                    long after = Instant.now().getEpochSecond();
                    assertThat(expSec)
                            .isBetween(before + EXP_SEC - 5, after + EXP_SEC + 5);
                })
                .verifyComplete();
    }

    @Test
    void generate_tokenTypeIsBearer() {
        StepVerifier.create(adapter.generate(dummyUser(), "CLIENTE"))
                .assertNext(tr -> assertThat(tr.tokenType()).isEqualTo("Bearer"))
                .verifyComplete();
    }
}
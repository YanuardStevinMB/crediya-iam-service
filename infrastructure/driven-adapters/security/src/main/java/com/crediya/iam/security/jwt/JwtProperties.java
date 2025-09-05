package com.crediya.iam.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    /** Secreto HMAC (>= 32 chars para HS256) */
    private String secret;

    /** (Opcional) issuer a validar */
    private String issuer;

    /** Tiempo de expiración en segundos */
    private Long expirationSec;

    // Getter y Setter para secret
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    // Getter y Setter para issuer
    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    // Getter y Setter para expirationSec
    public Long getExpirationSec() {
        return expirationSec;
    }

    public void setExpirationSec(Long expirationSec) {
        this.expirationSec = expirationSec;
    }
}

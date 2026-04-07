package org.example.ash.security.keycloak;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Creates a {@link NimbusJwtDecoder} pointed at Keycloak's JWKS endpoint.
 *
 * <p>The decoder fetches and caches Keycloak's public keys automatically,
 * so token verification does not require any locally stored key material.
 *
 * <p>Configure in {@code application.properties}:
 * <pre>
 *   keycloak.jwk-set-uri  = http://localhost:8180/realms/ash/protocol/openid-connect/certs
 *   keycloak.issuer-uri   = http://localhost:8180/realms/ash
 * </pre>
 */
@Configuration
public class KeycloakConfig {

    @Value("${keycloak.jwk-set-uri}")
    private String jwkSetUri;

    @Bean(name = "keycloakJwtDecoder")
    public NimbusJwtDecoder keycloakJwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }
}

package org.example.ash.security.keycloak;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ash.configuration.RequestContext;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Validates Keycloak-issued Bearer tokens for mobile clients.
 *
 * <p>This filter runs <em>after</em> {@link org.example.ash.security.JwtAuthenticationFilter}.
 * If the web-client filter already authenticated the request (local RS256 token),
 * this filter is a no-op. Otherwise it attempts Keycloak JWT validation.
 *
 * <pre>
 *   Mobile client                 Server filter chain
 *   ─────────────                 ─────────────────────────────────────────
 *   Authorization: Bearer <kc-token>
 *                            JwtAuthenticationFilter  →  fails (not local token)
 *                            KeycloakAuthenticationFilter  →  validates via JWKS
 *                                                         →  sets SecurityContext
 * </pre>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER        = "Bearer ";

    private final NimbusJwtDecoder                    keycloakJwtDecoder;
    private final KeycloakJwtAuthenticationConverter  converter;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/auth/")
            || path.startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        // ── Skip if already authenticated by the local JWT filter ──────────────
        Authentication existing = SecurityContextHolder.getContext().getAuthentication();
        if (existing != null && existing.isAuthenticated()) {
            chain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            chain.doFilter(request, response);
            return;
        }

        // ── Validate Keycloak token ────────────────────────────────────────────
        try {
            Jwt jwt = keycloakJwtDecoder.decode(token);
            var auth = converter.convert(jwt);
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Populate ThreadLocal so downstream code can call RequestContext.get()
            String username = jwt.getClaimAsString("preferred_username");
            RequestContext.set(username != null ? username : jwt.getSubject());

            log.debug("Keycloak authentication successful for user: {}", username);
        } catch (JwtException e) {
            // Not a valid Keycloak token – let Spring Security reject the request
            log.warn("Keycloak JWT validation failed: {}", e.getMessage());
        }

        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION);
        if (StringUtils.hasText(header) && header.startsWith(BEARER)) {
            return header.substring(BEARER.length());
        }
        return null;
    }
}

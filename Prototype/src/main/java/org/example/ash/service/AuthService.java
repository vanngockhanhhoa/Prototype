package org.example.ash.service;

import lombok.RequiredArgsConstructor;
import org.example.ash.dto.request.LoginRequest;
import org.example.ash.dto.request.RegisterRequest;
import org.example.ash.dto.response.LoginResponse;
import org.example.ash.entity.oracle.User;
import org.example.ash.exception.AppException;
import org.example.ash.repository.oracle.IUserRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final IUserRepo     userRepo;
    private final PasswordEncoder passwordEncoder;
    private final WebClient.Builder webClientBuilder;

    @Value("${keycloak.token-uri}")
    private String keycloakTokenUri;

    @Value("${keycloak.client-id}")
    private String clientId;

    // ── Login: delegate to Keycloak, return JWT ────────────────────────────────

    public LoginResponse login(LoginRequest request) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", clientId);
        form.add("username", request.getUsername());
        form.add("password", request.getPassword());

        Map<?, ?> tokenResponse = webClientBuilder.build()
                .post()
                .uri(keycloakTokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorMap(WebClientResponseException.class,
                        e -> new AppException("Invalid credentials").status(HttpStatus.UNAUTHORIZED.value()))
                .block();

        String token = (String) tokenResponse.get("access_token");
        return LoginResponse.builder()
                .token(token)
                .username(request.getUsername())
                .build();
    }

    // ── Register: persist user locally (Keycloak user management is separate) ─

    public LoginResponse register(RegisterRequest request) {
        if (userRepo.existsByUsername(request.getUsername())) {
            throw new AppException("Username already taken").status(HttpStatus.CONFLICT.value());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        userRepo.save(user);

        // After registration, log in via Keycloak to get a token
        return login(new LoginRequest() {{
            setUsername(request.getUsername());
            setPassword(request.getPassword());
        }});
    }
}

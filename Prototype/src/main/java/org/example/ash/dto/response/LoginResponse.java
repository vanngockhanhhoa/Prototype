package org.example.ash.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private final String token;
    private final String tokenType = "Bearer";
    private final String username;
}

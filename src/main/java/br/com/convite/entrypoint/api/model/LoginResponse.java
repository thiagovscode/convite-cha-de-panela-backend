package br.com.convite.entrypoint.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    /**
     * Campo legado mantido para compatibilidade total com frontends existentes.
     * Possui o mesmo valor que accessToken.
     */
    private String token;

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private String username;
    private String role;

    public LoginResponse(String token) {
        this.token = token;
        this.accessToken = token;
        this.tokenType = "Bearer";
    }
}

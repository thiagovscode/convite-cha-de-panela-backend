package br.com.convite.entrypoint.api.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Username é obrigatório")
    private String username;
    
    @NotBlank(message = "Password é obrigatório")
    private String password;
}

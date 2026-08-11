package br.com.convite.entrypoint.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RsvpRequest {
    @NotBlank(message = "O nome é obrigatório")
    private String name;
    
    @NotNull(message = "A confirmação é obrigatória")
    private Boolean confirmed;
    
    private String message;
}

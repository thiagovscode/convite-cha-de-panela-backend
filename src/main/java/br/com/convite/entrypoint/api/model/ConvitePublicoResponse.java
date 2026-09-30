package br.com.convite.entrypoint.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvitePublicoResponse {
    private String id;
    private String codigo;
    private String familia;
    private String telefone;
    private String status;
    private String papel;
    private List<MembroPublicoResponse> membros;
}

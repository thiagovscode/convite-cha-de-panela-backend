package br.com.convite.entrypoint.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembroPublicoResponse {
    private String id;
    private String nome;
    private Boolean criancaAte6Anos;
    private Boolean confirmadoRsvp;
}

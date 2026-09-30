package br.com.convite.entrypoint.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemAuditoriaMembroResponse {
    private String id;
    private String nome;
    private Boolean criancaAte6Anos;
    private Boolean confirmadoRsvp;
    private Boolean presenteCheckin;
    private LocalDateTime dataHoraCheckin;
    private String recepcionista;
    private String papel; // Ex: Padrinho, Madrinha, Pai dos Noivos, etc.
    private String vinculo; // Ex: Noivo, Noiva
    private Boolean participaCortejo;
}

package br.com.convite.gateway.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembroConviteEntity {
    private String id;
    private String nome;
    private Boolean criancaAte6Anos;
    private Boolean confirmadoRsvp;
    private Boolean presenteCheckin;
    private LocalDateTime dataHoraCheckin;
    private String recepcionista;
    private String papel; // Ex: Padrinho, Madrinha, Pai, Mãe, Daminha, Pajem
    private String vinculo; // Ex: Noivo, Noiva, Família, Amigo(a)
    private String par; // Par do cortejo
    private Boolean participaCortejo;
}

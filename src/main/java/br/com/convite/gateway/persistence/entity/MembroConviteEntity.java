package br.com.convite.gateway.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembroConviteEntity {
    @Id
    private UUID id;
    private String nome;
    private Boolean criancaAte6Anos;
    private Boolean confirmadoRsvp;
    private Boolean presenteCheckin;
    private LocalDateTime dataHoraCheckin;
    private String recepcionista;
    private String papel; // Ex: Padrinho, Madrinha, Pai, Mãe, Daminha, Pajem
    private String par; // Par do cortejo
    private Boolean participaCortejo;
}

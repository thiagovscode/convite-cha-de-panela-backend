package br.com.convite.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipanteCerimonia {
    private String id;
    private String nome;
    private String papel;
    private String vinculo;
    private String par;
    private String telefone;
    private String codigoConvite;
    private Boolean confirmadoRsvp;
    private Boolean presenteCheckin;
    private LocalDateTime dataHoraEntrada;
    private String statusCortejo; // AGUARDANDO_CHEGADA, NO_LOCAL, PRONTO_CORTEJO, ENTROU_CORTEJO
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

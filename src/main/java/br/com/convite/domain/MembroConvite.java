package br.com.convite.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembroConvite {
    private UUID id;
    private String nome;
    private Boolean criancaAte6Anos;
    private String papel;
    private String par;
    private Boolean participaCortejo;
    private Boolean confirmadoRsvp;
    private Boolean presenteCheckin;
    private LocalDateTime dataHoraCheckin;
    private String recepcionista;
}

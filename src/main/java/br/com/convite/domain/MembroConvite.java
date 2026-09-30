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
public class MembroConvite {
    private String id;
    private String nome;
    private Boolean criancaAte6Anos;
    private String papel;
    private String vinculo;
    private String par;
    private Boolean participaCortejo;
    private Boolean confirmadoRsvp;
    private Boolean presenteCheckin;
    private LocalDateTime dataHoraCheckin;
    private String recepcionista;
}

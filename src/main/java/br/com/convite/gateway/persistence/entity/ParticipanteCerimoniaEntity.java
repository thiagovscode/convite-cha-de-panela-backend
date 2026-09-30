package br.com.convite.gateway.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "participantes_cerimonia")
public class ParticipanteCerimoniaEntity {
    @Id
    private String id;

    private String nome;
    private String papel; // Madrinha, Padrinho, Pai dos Noivos, Mãe dos Noivos, Dama, etc.
    private String vinculo; // Noivo, Noiva, Casal
    private String par; // Nome do par no cortejo (ex: Mariana Alencar)
    private String telefone; // Telefone para contato
    private String codigoConvite; // Vinculo com o convite da família
    private Boolean confirmadoRsvp;
    private Boolean presenteCheckin;
    private LocalDateTime dataHoraEntrada;
    private String statusCortejo; // AGUARDANDO_CHEGADA, NO_LOCAL, PRONTO_CORTEJO, ENTROU_CORTEJO
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

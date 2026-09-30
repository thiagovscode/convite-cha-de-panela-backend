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
public class ItemAuditoriaFamiliaResponse {
    private String id;
    private String codigo;
    private String familia;
    private String statusRsvp; // PENDENTE, CONFIRMADO, RECUSADO
    private String telefone;
    private String papel; // Ex: Padrinhos dos Noivos, Pais dos Noivos, Família
    private int totalMembros;
    private int confirmadosRsvp;
    private int presentesCheckin;
    private int ausentesNoShow;
    private List<ItemAuditoriaMembroResponse> membros;
}

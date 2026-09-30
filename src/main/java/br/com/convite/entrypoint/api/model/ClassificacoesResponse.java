package br.com.convite.entrypoint.api.model;

import br.com.convite.domain.PapelParticipante;
import br.com.convite.domain.VinculoParticipante;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassificacoesResponse {
    private List<PapelParticipante> papeis;
    private List<VinculoParticipante> vinculos;
}

package br.com.convite.entrypoint.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricasResponse {
    // Convites (Famílias)
    private long totalConvites;
    private long totalConvitesConfirmados;
    private long totalConvitesRecusados;
    private long totalConvitesPendentes;

    // Pessoas (Convidados individuais)
    private long totalPessoas;
    private long totalConfirmados;
    private long totalRecusaram;
    private long totalPendentes;

    // Categorias de confirmados
    private long totalAdultosConfirmados;
    private long totalCriancasConfirmadas;

    // Taxas percentuais
    private double taxaConfirmacao;
    private double taxaRecusa;
    private double taxaPendentes;
    private double taxaPresencaRespondidos;
}

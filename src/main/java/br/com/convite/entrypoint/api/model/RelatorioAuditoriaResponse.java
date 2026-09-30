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
public class RelatorioAuditoriaResponse {
    // 1. Total Planejado pelos Noivos (Lista de Convidados)
    private long totalConvidadosPrevistos;
    private long totalAdultosPrevistos;
    private long totalCriancasPrevistas;

    // 2. Total Confirmado no RSVP prévio pelo Convidado
    private long totalConfirmadosRsvp;
    private long totalAdultosConfirmados;
    private long totalCriancasConfirmadas;

    // 3. Realidade no Dia da Festa (Portaria / Check-in)
    private long totalPresentesReais;
    private long totalAdultosPresentes; // Pagantes integrais do buffet
    private long totalCriancasPresentes; // Menores de 7 anos

    // 4. Quebras / Divergências
    private long totalAusentesNoShow; // Quem confirmou que ia, mas não apareceu
    private long totalAguardandoChegada; // Quem confirmou e ainda não passou na portaria
    private long totalRecusados; // Quem avisou no RSVP que não ia

    private List<ItemAuditoriaFamiliaResponse> familias;
}

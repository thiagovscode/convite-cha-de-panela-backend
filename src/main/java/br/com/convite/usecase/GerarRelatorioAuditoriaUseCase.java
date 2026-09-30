package br.com.convite.usecase;

import br.com.convite.domain.Convite;

import java.util.List;

public interface GerarRelatorioAuditoriaUseCase {

    record RelatorioAuditoria(
            long totalConvidadosPrevistos,
            long totalAdultosPrevistos,
            long totalCriancasPrevistas,
            long totalConfirmadosRsvp,
            long totalAdultosConfirmados,
            long totalCriancasConfirmadas,
            long totalPresentesReais,
            long totalAdultosPresentes,
            long totalCriancasPresentes,
            long totalAusentesNoShow,
            long totalAguardandoChegada,
            long totalRecusados,
            List<Convite> convites
    ) {}

    RelatorioAuditoria executar();
}
